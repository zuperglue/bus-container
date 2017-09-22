package com.zuperglue.bus.services.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuperglue.bus.App;
import com.zuperglue.bus.services.subscription.Subscription;
import com.zuperglue.bus.services.subscription.SubscriptionService;
import com.zuperglue.bus.services.resources.ResourceManager;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class HealthController {

    @Value("${server.port}")
    private String port;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    ResourceManager resourceManager;


    @RequestMapping(value = "/health", method = RequestMethod.GET)
    Health health() {

        resourceManager.submitTask(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("submiiting health task " + threadName);
            String submitUrl = "http://localhost:" + port + "/bus/subscribe/" + App.NAME;
            String callbackUrl = "http://localhost:" + port + "/bus/message";
            Subscription subscription = new Subscription(callbackUrl);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String subsctionAsStr = mapper.writeValueAsString(subscription);
                String response = resourceManager.post(submitUrl, subsctionAsStr);
                System.out.println("Got Response: \"" + response + "\" thread: " + threadName);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        subscriptionService.publish("Hello");

        return new Health("Okiee dokie");
    }

}
