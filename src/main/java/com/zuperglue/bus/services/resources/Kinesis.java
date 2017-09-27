package com.zuperglue.bus.services.resources;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.InitialPositionInStream;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class Kinesis implements ShutdownHandler.Aware {

    private  Log LOG = LogFactory.getLog(Kinesis.class);


    private final List<String> streams;
    private final ConcurrentHashMap<String,WorkerHolder> workers;

    private final IRecordProcessorFactory recordProcessorFactory;
    private final AWSCredentialsProvider credentialsProvider;
    private final AmazonKinesis amazonKinesisClient;

    private final String WORKER_POOL_NAME = "kinesis-workers";
    private final String PUT_POOL_NAME = "kinesis-put";
    private final ExecutorService workerThreadPool;
    private final ExecutorService putThreadPool;


    public Kinesis(@Value("${kinesis.streams}") String streamNames,
                   @Autowired ShutdownHandler shutdownHandler,
                   @Autowired ThreadPools threadPools,
                   @Value("${kinesis.recordProcessor.charset}") String charsetName)
    {

        // Enable streams we can access...
        streams = new ArrayList<>();
        if (streamNames != null) {
            String[] names = streamNames.split(",");
            for (String streamName : names) {
                if (!streams.contains(streamName.trim())) {
                    LOG.info("Enabling Kinesis stream: '"+streamName.trim()+"'");
                    streams.add(streamName.trim());
                }
            }
        }

        // Ensure the JVM will refresh the cached IP values of AWS resources (e.g. service endpoints).
        java.security.Security.setProperty("networkaddress.cache.ttl", "60");
        credentialsProvider = new DefaultAWSCredentialsProviderChain();
        LOG.info("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI: "+ System.getenv("AWS_CONTAINER_CREDENTIALS_RELATIVE_URI"));
        LOG.info("AWS_CONTAINER_CREDENTIALS_FULL_URI: "+ System.getenv("AWS_CONTAINER_CREDENTIALS_FULL_URI"));

        //credentialsProvider = new EC2ContainerCredentialsProviderWrapper();

        recordProcessorFactory = new RecordProcessorFactory(charsetName);
        workers = new ConcurrentHashMap<>();

        workerThreadPool = threadPools.createPool(WORKER_POOL_NAME, 5);
        putThreadPool = threadPools.createPool(PUT_POOL_NAME, 20);
        amazonKinesisClient = AmazonKinesisClientBuilder
                .standard()
                .withCredentials(credentialsProvider)
                .build();

        shutdownHandler.notifyMeAtShutdown("Kinesis", this, ShutdownHandler.Priority.FIRST);
    }

    public List<String> getStreams(){return streams;}

    public List<WorkerHolder> getWorkers(){
        List<WorkerHolder> result = new ArrayList<>();
        for (Map.Entry<String,WorkerHolder> entry : workers.entrySet()){
            result.add(entry.getValue());
        }
        return result;
    }

    public void putMessage(String streamName, String message) throws AmazonClientException {
        LOG.info("Put Message: " + message);
        if (streamName == null || !streams.contains(streamName)){
            throw new AmazonClientException("Unknown stream name: "+ ((streamName!=null)?streamName : "NULL"));
        }
        byte[] bytes = (message != null) ? message.getBytes() : null;
        if (bytes == null) {
            LOG.error("No message !!!");
            return;
        }

        PutRecordRequest putRecord = new PutRecordRequest();
        putRecord.setStreamName(streamName);
        putRecord.setPartitionKey(UUID.randomUUID().toString());
        putRecord.setData(ByteBuffer.wrap(bytes));
        amazonKinesisClient.putRecord(putRecord);
        LOG.debug("Put Message done...");
    }

    public void submitMessage(String streamName, String message){
        putThreadPool.submit(() -> {
            LOG.debug("Submit Message: " + message);
            try {
                putMessage(streamName, message);
            } catch (AmazonClientException ex) {
                LOG.error("Error sending record to Amazon Kinesis." + ex);
            }
        });
    }

    public String getWorkerName(String streamName, String appName){
        return streamName+"."+appName;
    }


    public void startWorker(String streamName, String appName) {
        if (!streams.contains(streamName)){
            throw new AmazonClientException("Unknown stream name: "+streamName);
        }
        String workerName = getWorkerName(streamName,appName);
        if (!workers.containsKey(workerName)) {
            workers.put(workerName, new WorkerHolder(streamName,appName,null,null));

            workerThreadPool.submit(() -> {

                //Thread.currentThread().setName("worker-" + appName);
                LOG.info("Starting worker : " + workerName);

                // Create a workerID
                String workerId = null;
                try {
                    workerId = workerName+":"+InetAddress.getLocalHost().getCanonicalHostName() + ":" + UUID.randomUUID();
                } catch (UnknownHostException e) {
                    workerId = workerName+":"+"UNKNOWN_HOST:" + UUID.randomUUID();
                }

                // Try to verify AWS credentials
                try {
                    credentialsProvider.getCredentials();
                } catch (Exception e) {
                    throw new AmazonClientException("Cannot load AWS credentials", e);
                }

                try {
                    // Create a Kinesis Client config
                    KinesisClientLibConfiguration config = new KinesisClientLibConfiguration(
                            workerName,  // Make uniquie
                            streamName,
                            credentialsProvider,
                            workerId
                    );
                    config.withIdleTimeBetweenReadsInMillis(1000L);
                    config.withInitialPositionInStream(InitialPositionInStream.TRIM_HORIZON);

                    // Create and start Worker
                    Worker worker = new Worker.Builder()
                            .recordProcessorFactory(recordProcessorFactory)
                            .config(config)
                            .build();
                    workers.put(workerName, new WorkerHolder(streamName,appName,worker,workerId));
                    worker.run();
                    LOG.info("Worker thread is exiting...");
                } catch (Exception e) {
                    throw new AmazonClientException("Cannot start Kinesis Worker", e);
                }

            });

        } else {
            LOG.info("Worker already started: " + workerName);
        }
    }

    public void stopWorker(String workerName){
        if (workers.containsKey(workerName)) {
            WorkerHolder holder = workers.get(workerName);
            Worker worker = holder.worker;
            if (worker != null) {
                Future<Boolean> result = worker.startGracefulShutdown();
                try {
                    LOG.info("Stop Worker " + workerName + " result: " + result.get());
                } catch (InterruptedException e) {
                    LOG.error("Stop Worker " + workerName + " interrupted");
                } catch (ExecutionException e) {
                    LOG.error("Stop Worker " + workerName + " exception: " + e.getMessage());
                }
            }
            workers.remove(workerName);
        } else {
            LOG.warn("Stop unknown Worker: " + workerName);
        }
    }


    public static class RecordProcessorFactory implements IRecordProcessorFactory {

        final String charsetName;

        public RecordProcessorFactory(String charsetName) {
            super();
            this.charsetName = charsetName;
        }

        @Override
        public IRecordProcessor createProcessor() {return new RecordProcessor(charsetName);}

    }


    @Override
    public void shutdownNotification() {
        LOG.info("Kinesis shutdown start");
        shutdown();
        LOG.debug("Kinesis shutdown complete");
    }

    public void shutdown(){
        ConcurrentHashMap<String, Future<Boolean>> shutDownResult = new ConcurrentHashMap<>();
        // Start shutdown of workers.
        for (Map.Entry<String, WorkerHolder> entry : workers.entrySet()) {
            String workerName = entry.getKey();
            WorkerHolder holder = entry.getValue();
            if (holder.worker != null) {
                shutDownResult.put(workerName, holder.worker.startGracefulShutdown());
            }
        }
        // Get results
        for (Map.Entry<String, Future<Boolean>> entry : shutDownResult.entrySet()) {
            String workerName = entry.getKey();
            Future<Boolean> result = entry.getValue();
            try {
                LOG.info("Shutdown of Worker : " + workerName + " result: " + result.get());
            } catch (InterruptedException e) {
                LOG.error("Shutdown of worker " + workerName + " interrupted");
            } catch (ExecutionException e) {
                LOG.error("Shutdown of worker " + workerName + " exception: " + e.getMessage());
            }
        }
        workers.clear();
        amazonKinesisClient.shutdown();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class WorkerHolder {
        String streamName;
        String appName;
        String workerId;
        Worker worker;

        public WorkerHolder(String streamName, String appName, Worker worker, String workerId){
            this.streamName = streamName;
            this.appName = appName;
            this.worker = worker;
            this.workerId = workerId;
        }
        public String getStreamName(){ return streamName;};
        public String getAppName(){ return appName;};
        public String getWorkerId(){ return workerId;};

    }
}
