package com.zuperglue.bus.services.subscription;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}subscribe" )
public class SubscribeController {

    private Log LOG = LogFactory.getLog(SubscribeController.class);

    @Autowired
    SubscriptionService subscriptionService;

    @RequestMapping(value = "/{service}", method = RequestMethod.POST)
    String subscribe(@PathVariable String service, @RequestBody String payload) throws IOException {
        LOG.info("Subscriber payload: "+payload);

        ObjectMapper mapper = new ObjectMapper();
        SubscriptionRequest subscriptionReq = mapper.readValue(payload, SubscriptionRequest.class);
        subscriptionService.subscribe(service,subscriptionReq);
        return "Subscribe to "+ service;
    }

    @RequestMapping(value = "/{service}", method = RequestMethod.DELETE)
    String unsubscripe(@PathVariable String service) {
        return "Unsubscribe to " + service;
    }

    @RequestMapping(value = "/{service}", method = RequestMethod.GET)
    String subscription(@PathVariable String service) {
        return "Subscriptions info for "+ service;
    }

}
