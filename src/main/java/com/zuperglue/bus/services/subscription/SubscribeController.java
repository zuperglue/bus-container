package com.zuperglue.bus.services.subscription;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}subscribe" )
public class SubscribeController {

    @RequestMapping(value = "/{service}", method = RequestMethod.POST)
    String subscribe(@PathVariable String service) {
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
