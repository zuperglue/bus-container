package com.zuperglue.bus.services.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

/**
 * Created by zuperglue on 2017-09-24.
 */
@Component
public class ShutdownHandler {

    private Log LOG = LogFactory.getLog(ShutdownHandler.class);


    public enum Priority {
        FIRST, MIDDLE, LAST
    }


    public interface Aware {
        public void shutdownNotification();
    }

    private static class ClientHolder {
        public Priority priority;
        public Aware client;
        public ClientHolder(Priority prio, Aware client){
            this.priority = prio;
            this.client = client;
        }
    }

    private final ConcurrentHashMap<String,ClientHolder> handlers;

    public ShutdownHandler(){
        LOG.info("ShutdownHandler create");
        handlers = new ConcurrentHashMap();
    }

    public void notifyMeAtShutdown(String name, Aware client, Priority prio){
        LOG.info("ShutdownHandler notifyMeAtShutdown registation: " + name);
        handlers.put(name,new ClientHolder(prio,client));
    }

    @PreDestroy
    public  void shutdown(){
        // First run
        LOG.info("- Shutdown notification: Priority.FIRST -------------");
        for (Map.Entry<String,ClientHolder> entry : handlers.entrySet()){
            String clientName = entry.getKey();
            ClientHolder holder = entry.getValue();
            if (holder.priority == Priority.FIRST) {
                holder.client.shutdownNotification();
            }
        }
        // Second run
        LOG.info("- Shutdown notification: Priority.MIDDLE -------------");
        for (Map.Entry<String,ClientHolder> entry : handlers.entrySet()){
            String clientName = entry.getKey();
            ClientHolder holder = entry.getValue();
            if (holder.priority == Priority.MIDDLE) {
                holder.client.shutdownNotification();
            }
        }
        // Last run
        LOG.info("- Shutdown notification: Priority.LAST -------------");
        for (Map.Entry<String,ClientHolder> entry : handlers.entrySet()){
            String clientName = entry.getKey();
            ClientHolder holder = entry.getValue();
            if (holder.priority == Priority.LAST) {
                holder.client.shutdownNotification();
            }
        }
        // Release
        handlers.clear();

    }
}
