package com.zuperglue;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zuperglue on 2017-09-20 .
 */

@RestController
@RequestMapping( "${CONTAINER_PATH}" )
public class RootController {

    @RequestMapping("/")
    String root() {
        return "Hello world....";
    }

    @RequestMapping("/health")
    String health() {
        return "Healthy...";
    }

    @RequestMapping("/throw")
    String throwE() throws IllegalArgumentException{
        throw new IllegalArgumentException("Error...");
        //return "Healthy...";
    }

    @RequestMapping("/halt")
    String halt() {
        System.exit(1);
        return "";
    }


}
