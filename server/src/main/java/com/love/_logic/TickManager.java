package com.love._logic;

import com.love._api.Api;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._core.Room;
import com.love._evt.RoomTickEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class TickManager {
    private long tick;
    private final long createTime = now();
    private long preFrameTime = -1L;
    private long lastFrameTime = -1L;
    final Room room;
    final ConcurrentLinkedDeque<Runnable> queue;
    private final Listener tickHandler = (env)->{onTick(RoomTickEvent.class.cast(env));};
    public TickManager(Room room) {
        this.room = room;
        queue = new ConcurrentLinkedDeque<>();
        room.roomDispatcher().on(Event.Type.ROOM_TICK,tickHandler);
    }
    long now(){ return Api.now();}
    void onTick(RoomTickEvent e) {
        tick++;
        if(-1L==preFrameTime){
            preFrameTime = lastFrameTime = e.getLast();
        }else{
            preFrameTime = lastFrameTime;
            lastFrameTime = e.getLast();
        }
        Runnable r;
        while(Objects.nonNull(r=queue.poll()))r.run();
    }
    public float getDelta(){
        return (lastFrameTime-preFrameTime)/1000.f;
    }
    public long getTick(){
        return tick;
    }
    public long aliveTime(){
        return lastFrameTime - createTime;
    }
}
