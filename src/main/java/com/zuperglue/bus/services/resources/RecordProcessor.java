package com.zuperglue.bus.services.resources;

import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.v2.IShutdownNotificationAware;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.ShutdownReason;
import com.amazonaws.services.kinesis.clientlibrary.types.InitializationInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ProcessRecordsInput;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownInput;
import com.amazonaws.services.kinesis.model.Record;

/**
 * Created by zuperglue on 2017-09-22.
 */
//https://github.com/aws/aws-sdk-java/blob/master/src/samples/AmazonKinesis/AmazonKinesisApplicationSampleRecordProcessor.java

public class RecordProcessor implements IRecordProcessor,IShutdownNotificationAware {

    private static final Log LOG = LogFactory.getLog(Kinesis.class);
    private String kinesisShardId;
    private InitializationInput initializationInput;

    // Backoff and retry settings
    private static final long BACKOFF_TIME_IN_MILLIS = 3000L;
    private static final int NUM_RETRIES = 10;

    // Checkpoint about once a minute
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L;
    private long nextCheckpointTimeInMillis;

    private final CharsetDecoder decoder;

    public RecordProcessor(String charsetName){
        //String charsetName = "UTF-8";
        decoder = Charset.forName(charsetName).newDecoder();
    }

    @Override
    public void initialize(InitializationInput initializationInput) {
        LOG.info("Initializing record processor for shard: " + initializationInput.getShardId());
        this.kinesisShardId = initializationInput.getShardId();
        this.initializationInput = initializationInput;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRecords(ProcessRecordsInput processRecordsInput) {
        LOG.info("Start processing " + processRecordsInput.getRecords().size() + " records from " + kinesisShardId);

        // Process records and perform all exception handling.

        // Checkpoint once every checkpoint interval.
        checkpoint(processRecordsInput.getCheckpointer());
        LOG.info("Done processing: " + processRecordsInput.getRecords().size() + " records from " + kinesisShardId);
    }


    public String getData(Record record) throws CharacterCodingException{
        return decoder.decode(record.getData()).toString();
    }

    @Override
    public void shutdownRequested(IRecordProcessorCheckpointer recordProcessorCheckpointer) {
        LOG.info("ShutdownRequested for shard: " + kinesisShardId);
        // Needs coordination with processRecords???
        checkpoint(recordProcessorCheckpointer,true);
    }

    @Override
    public void shutdown(ShutdownInput shutdownInput) {
        LOG.info("Shutting down record processor for shard: " + kinesisShardId);
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (shutdownInput.getShutdownReason() == ShutdownReason.TERMINATE) {
            checkpoint(shutdownInput.getCheckpointer(),true);
        }
    }

    /** Checkpoint with retries where checkpoint is performed only if CHECKPOINT_INTERVAL is passed
     * @param checkpointer
     */
    public void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        checkpoint(checkpointer,false);
    }


    /** Checkpoint with retries.
     * @param checkpointer
     * @param forced
     */
    public void checkpoint(IRecordProcessorCheckpointer checkpointer, boolean forced) {
        // Test if we passed time to make checkpoint
        if (forced || System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            LOG.info("Checkpointing shard " + kinesisShardId);
            for (int i = 0; i < NUM_RETRIES; i++) {
                try {
                    checkpointer.checkpoint();
                    break;
                } catch (ShutdownException se) {
                    // Ignore checkpoint if the processor instance has been shutdown (fail over).
                    LOG.info("Caught shutdown exception, skipping checkpoint.", se);
                    break;
                } catch (ThrottlingException e) {
                    // Backoff and re-attempt checkpoint upon transient failures
                    if (i >= (NUM_RETRIES - 1)) {
                        LOG.error("Checkpoint failed after " + (i + 1) + "attempts.", e);
                        break;
                    } else {
                        LOG.info("Transient issue when checkpointing - attempt " + (i + 1) + " of "
                                         + NUM_RETRIES, e);
                    }
                } catch (InvalidStateException e) {
                    // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
                    LOG.error("Cannot save checkpoint to the DynamoDB table used by the Amazon Kinesis Client Library.", e);
                    break;
                }
                try {
                    Thread.sleep(BACKOFF_TIME_IN_MILLIS);
                } catch (InterruptedException e) {
                    LOG.debug("Interrupted sleep", e);
                }
            }
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        } else {
            LOG.info("Checkpointing skipped due to CHECKPOINT_INTERVAL not passed " + kinesisShardId);
        }
    }


}
