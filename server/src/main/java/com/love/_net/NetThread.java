package com.love._net;

import com.love._api.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetThread {
    static final  Thread main;
    static final Thread thread;
    static final Logger logger = LoggerFactory.getLogger(NetThread.class);
    static final ConcurrentLinkedQueue<Runnable> queue;
    static boolean enqueue(Runnable runnable){
        return queue.offer(runnable);
    }
    static{
        main = Thread.currentThread();
        queue = new ConcurrentLinkedQueue<>();
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Runnable runner;
                while(true){
                    try{
                        while(Objects.nonNull(runner=queue.poll()))runner.run();
                    }catch(Throwable t){
                        logger.error(t.getMessage(),t);
                    }
                    Thread.yield();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    static long now(){ return Api.now();}

}
