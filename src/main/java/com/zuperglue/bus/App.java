package com.zuperglue.bus;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@EnableCaching
public class App  {

    public static final String NAME = "bus";

    public static void main( String[] args ) {
        String port= System.getenv("PORT");
        System.out.print("Starting Port:" + port);
        ApplicationContext context = SpringApplication.run(App.class, args);
    }

}


