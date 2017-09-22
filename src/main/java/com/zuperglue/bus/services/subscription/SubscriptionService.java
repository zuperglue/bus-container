package com.zuperglue.bus.services.subscription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.zuperglue.bus.services.resources.ResourceManager;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class SubscriptionService {

    @Autowired
    ResourceManager resourceManager;

    private ConcurrentHashMap<String,Subscription> subsciptions = new ConcurrentHashMap();

    public SubscriptionService(){

    }

    public void subscribe(String name,Subscription subscription){
        if (!subsciptions.containsKey(name)){
            subsciptions.put(name,subscription);
        }
    }

    public void publish(String message) {
        for (Map.Entry<String,Subscription> entry : subsciptions.entrySet()){
            Subscription subscription = entry.getValue();
            resourceManager.submitTask(() -> {
                resourceManager.post(subscription.callback, message);
            });
        }
    }

}
