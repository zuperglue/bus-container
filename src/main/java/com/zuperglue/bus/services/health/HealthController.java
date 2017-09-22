package com.zuperglue.bus.services.health;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.zuperglue.bus.App;

/**
 * Created by zuperglue on 2017-09-22.
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class HealthController {


    @RequestMapping(value = "/health", method = RequestMethod.GET)
    Health health() {

        App.executor.submit(() -> {
            String threadName = Thread.currentThread().getName();
            System.out.println("submiiting health task " + threadName);

        });
        return new Health("Okiee dokie");
    }

}
