package com.zuperglue.bus.services.resources;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by zuperglue on 2017-09-22.
 */
@Component
public class ThreadPools implements ShutdownHandler.Aware {

    private Log LOG = LogFactory.getLog(ThreadPools.class);

    private final ConcurrentHashMap<String,ExecutorService> pools;

    private final String standardPool = "threadpool";


    public ThreadPools(@Value("${server.threadPoolSize}") Integer poolSize,
                       @Autowired ShutdownHandler shutdownHandler){
        int size = (poolSize != null) ? poolSize : 10;
        pools = new ConcurrentHashMap();

        ExecutorService executor = Executors.newFixedThreadPool(size, new NamedThreadFactory(
                standardPool + "(" + size + ")"));
        pools.put(standardPool,executor);
        shutdownHandler.notifyMeAtShutdown("ThreadPools", this, ShutdownHandler.Priority.MIDDLE);
    };

    public ExecutorService createPool(String name, int size){
        ExecutorService executor = Executors.newFixedThreadPool(size, new NamedThreadFactory(name + "(" + size + ")"));
        pools.put(name, executor);
        return executor;
    }

    public void submitTask(Runnable task){
        submitTask(standardPool,task);
    }

    public void submitTask(String poolName, Runnable task){
        ExecutorService executor = pools.get(poolName);
        if (executor != null) {
            executor.submit(task);
        } else {
            LOG.warn("Unknown pool: "+poolName);
        }
    }


    @Override
    public void shutdownNotification() {
        for (Map.Entry<String,ExecutorService> entry : pools.entrySet()){
            String poolName = entry.getKey();
            shutdownPool(poolName);
        }
    }

    private void shutdownPool(String name){
        ExecutorService executor = pools.get(name);
        if (executor != null) {
            LOG.info("ThreadPools shutdown: "+ name);
            executor.shutdown();
            try {
                final boolean done = executor.awaitTermination(15, TimeUnit.SECONDS);
                if (!done)
                    executor.shutdownNow();
            } catch (InterruptedException skip) {
                executor.shutdownNow();
            }
        } else {
            LOG.warn("Unknown pool: "+name);
        }
    }

    static class NamedThreadFactory implements ThreadFactory {
        static final AtomicInteger poolNumber = new AtomicInteger(1);
        final ThreadGroup group;
        final AtomicInteger threadNumber = new AtomicInteger(1);
        final String namePrefix;

        NamedThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null)? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            //namePrefix = prefix +"-" + poolNumber.getAndIncrement() + "-thread-";
            namePrefix = prefix + "-thread-";

        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
