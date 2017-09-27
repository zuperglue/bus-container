package com.zuperglue.bus.services.health;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zuperglue.bus.App;
import com.zuperglue.bus.services.resources.Kinesis;
import com.zuperglue.bus.services.resources.ThreadPools;
import com.zuperglue.bus.services.subscription.SubscriptionRequest;
import com.zuperglue.bus.services.subscription.SubscriptionService;
import com.zuperglue.bus.services.resources.HttpPool;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class HealthController {

    private Log LOG = LogFactory.getLog(HealthController.class);


    @Value("${server.port}")
    private String port;

    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    HttpPool httpPool;

    @Autowired
    ThreadPools threadPool;

    @Autowired
    Kinesis kinesis;

    @RequestMapping(value = "/health", method = RequestMethod.GET)
    Health health() {


            /*
             threadPool.submitTask(() -> {
            LOG.info("submiting health task ");
            String submitUrl = "http://localhost:" + port + "/bus/subscribe/" + App.NAME;
            String callbackUrl = "http://localhost:" + port + "/bus/message";
            SubscriptionRequest subscription = new SubscriptionRequest(callbackUrl);
            try {
                ObjectMapper mapper = new ObjectMapper();
                String subsctionAsStr = mapper.writeValueAsString(subscription);
                String response = httpPool.post(submitUrl, subsctionAsStr);
                LOG.info("Got Response: \"" + response);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            LOG.info("Health task done...");
                        try {
                //kinesis.startWorker("test-stream", "bus");
            } catch (Exception e){
                LOG.error("Error starting Kinesis worker bus",e);
            }
        });
            */
        try {
            kinesis.startWorker("zg-dev", "bus");
        } catch (Exception e) {
            LOG.error("Error starting Kinesis worker bus", e);
        }
        //LOG.info("Health OK");
        return new Health("Okiee dokie - 7");
    }

}

