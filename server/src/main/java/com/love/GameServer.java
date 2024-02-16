package com.love;

import com.love._api.EventDispatcher;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.*;
import com.love._logic.InstanceManager;
import com.love._net.*;
import com.love._api.NetOperator;
import com.love._api.UserOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class GameServer implements Server {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
    final TopEventDispatcher dispatcher = new TopEventDispatcher();
    final InstanceManager instanceManager = new InstanceManager(this);
    final NetManager netManager = new NetManager(this);
    final UserManager userManager = new UserManager(this);
    boolean running;
    final Listener tickListener = (evt)-> onTick();
    public void start(){
        dispatcher.on(Event.Type.SYS_TICK,tickListener);
        dispatcher.on(Event.Type.SYS_QUIT_REQ,evt-> {
            logger.info("{} request quit",SysQuitEvent.class.cast(evt).who);
            running=false;
        });
        dispatcher.emit(new SysStartEvent());
        final SysTickEvent tick = new SysTickEvent();
        {
            running = true;
            logger.info("GameServer.start");
            while(running){
                try{
                    dispatcher.emit(tick.reuse());
                    Thread.sleep(1000);
                }catch (Throwable t){
                    logger.error(t.getMessage(),t);
                }
            }
        }
        dispatcher.emit(new SysStopEvent());
        dispatcher.off(Event.Type.SYS_TICK,tickListener);
        onTick();//clear the queue's tasks
        dispatcher.emit(new SysNullEvent());//last trigger the concurrency logic;
        final Function<Map<Event.Type, List<Listener>>,Integer> fun = c->c.values().stream().flatMap(Collection::stream).toList().size();
        final Map<Event.Type, List<Listener>> remain = new HashMap<>();
        dispatcher.channels(remain);
        int size;
        size = fun.apply(remain);
        if(1!=size)logger.error("1,size:{}\ndetail:{}",size,remain);
        remain.clear();
        dispatcher.onTemp(remain);
        size = fun.apply(remain);
        if(0!=size)logger.error("2,size:{}\ndetail:{}",size,remain);
        remain.clear();
        dispatcher.offTemp(remain);
        size = fun.apply(remain);
        if(0!=size)logger.error("3,size:{}\ndetail:{}",size,remain);
        remain.clear();
    }
    void onTick(){
        Runnable runner;
        while(Objects.nonNull(runner=queue.poll()))runner.run();
    }
    @Override
    public EventDispatcher dispatcher() {
        return dispatcher;
    }

    @Override
    public UserOperator userOperator() {
        return userManager;
    }

    @Override
    public NetOperator netOperator() {
        return netManager;
    }

    @Override
    public boolean post(Runnable runnable) {
        if(Objects.isNull(runnable))return false;
        return queue.offer(runnable);
    }
}

