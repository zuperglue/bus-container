package com.zuperglue.bus.services.resources;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class ResourceManager {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public ResourceManager(){}

    public void submitTask(Runnable task){
        executor.submit(task);
    }

    public String post(String url, String message){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity <String> (message, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url, httpEntity, String.class);
    }

}
