package com.zuperglue.bus.services.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class HttpPool implements ShutdownHandler.Aware {

    private Log LOG = LogFactory.getLog(HttpPool.class);

    //TODO: Use a real http connection pool

    public HttpPool(@Autowired ShutdownHandler shutdownHandler){
        shutdownHandler.notifyMeAtShutdown("HttpPool", this, ShutdownHandler.Priority.LAST);
    }

    public String post(String url, String message){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", "application/json");
        HttpEntity<String> httpEntity = new HttpEntity <String> (message, httpHeaders);
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject(url, httpEntity, String.class);
    }

    @Override
    public void shutdownNotification() {
        LOG.info("HttpPool shutdown");
    }
}
