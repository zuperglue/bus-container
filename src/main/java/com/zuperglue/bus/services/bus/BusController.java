package com.zuperglue.bus.services.bus;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zuperglue.bus.services.resources.Kinesis;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class BusController {

    @Autowired
    Kinesis kinesis;

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

    @RequestMapping(value = "/workers", method = RequestMethod.GET)
    List<Kinesis.WorkerHolder> worker() {
       return kinesis.getWorkers();
    }

    @RequestMapping(value = "/streams", method = RequestMethod.GET)
    List<String> streams() {
        return kinesis.getStreams();
    }


}
