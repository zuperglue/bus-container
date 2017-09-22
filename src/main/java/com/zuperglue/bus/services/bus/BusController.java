package com.zuperglue.bus.services.bus;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class BusController {

    @RequestMapping(value = "/", method = RequestMethod.POST)
    String subscribe() {
        return "1) bus send....";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    String subscriptions() {
        return "BUS....";
    }

    @RequestMapping(value = "/message", method = RequestMethod.POST)
    String message() {
        System.out.println("Got message...");
        return "Got message...";
    }


}
