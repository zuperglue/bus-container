package com.zuperglue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@Configuration
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@EnableCaching
public class App  {

    public static void main( String[] args ) {
        String port= System.getenv("PORT");
        System.out.print("Starting Port:"+port);
        SpringApplication.run(App.class, args);
    }
}
