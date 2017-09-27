package com.zuperglue.bus.services.subscription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.zuperglue.bus.services.resources.HttpPool;
import com.zuperglue.bus.services.resources.Kinesis;
import com.zuperglue.bus.services.resources.ThreadPools;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class SubscriptionService {

    private Log LOG = LogFactory.getLog(SubscriptionService.class);


    @Autowired
    HttpPool httpPool;

    @Autowired
    ThreadPools threadPool;

    @Autowired
    Kinesis kinesis;

    private final ConcurrentHashMap<String,SubscriptionRequest> subsciptions;

    public SubscriptionService(){
        subsciptions = new ConcurrentHashMap();

    }

    public void subscribe(String name,SubscriptionRequest subscriptionReq){
        if (!subsciptions.containsKey(name)){
            subsciptions.put(name, subscriptionReq);
            try {
                kinesis.startWorker("test-stream", name);
            } catch (AmazonClientException e){
                LOG.error("Could not start Worker "+ e);
            }
        }
        kinesis.submitMessage("test-stream","Hello world...");

    }

    public void publish(String message) {
        for (Map.Entry<String,SubscriptionRequest> entry : subsciptions.entrySet()){
            SubscriptionRequest subscription = entry.getValue();
            threadPool.submitTask(() -> {
                httpPool.post(subscription.callback, message);
            });
        }
    }

}
