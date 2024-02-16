package com.love._logic;

import com.love._api.EventDispatcher;
import com.love._core.Room;
import com.love._api.Server;
import com.love._evt.InstanceEventDispatcher;
import com.love._evt.RoomStartEvent;
import com.love._evt.RoomStopEvent;
import com.love._evt.RoomTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Instance implements Runnable, Room {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int id;
    private String name;
    final Server server;
    private final EventDispatcher roomChannel;
    private final TerrainManager terrainManager;
    private final ActorManager actorManager;
    private final SpaceActorManager spaceActorManager;
    private final TickManager tickManager;
    private final Thread main;
    private final Thread worker;
    private boolean running;
    public Instance(int id,String name,Server server) {
        this.id = id;
        this.name = name;
        this.server = server;
        this.roomChannel = new InstanceEventDispatcher();
        main = Thread.currentThread();
        worker = new Thread(this);
        terrainManager = new TerrainManager(this);
        actorManager = new ActorManager(this);
        spaceActorManager = new SpaceActorManager(this);
        tickManager = new TickManager(this);
        worker.start();
        while(!worker.isAlive())Thread.yield();
    }
    public boolean isFinish() {
        return !running;
    }
    public void clear() {
        if(Thread.currentThread()!=main)return;
        if(!isFinish())return;
        do{
            try {
                worker.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }while(worker.isAlive());
    }
    public int getId() {
        return id;
    }
    void onAction(){
        class Action{
            void onJoin(){}
            void onLeave(){}
        }
        running = false;
    }
    @Override
    public void run() {
        if(Thread.currentThread()!=worker)return;
        running = true;
        RoomTickEvent tick = new RoomTickEvent();
        roomChannel.emit(new RoomStartEvent());
        while(running){
           try{
               roomChannel.emit(tick.reuse());
           }catch (Throwable t){
               logger.error(t.getMessage(),t);
           }
        }
        roomChannel.emit(new RoomStopEvent());
    }
    public InstanceInfo getInfo() {
        return new InstanceInfo(id,name);
    }
    @Override
    public EventDispatcher roomDispatcher() {
        return roomChannel;
    }
    @Override
    public Server server() {
        return server;
    }
    @Override
    public boolean roomPost(Runnable run) {
        if(Objects.isNull(run))return false;
        return tickManager.queue.offer(run);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(() -> {
            System.out.println("1");
        });
        thread.start();
        while(!thread.isAlive()){
            System.out.println("Instance.main");
        }
        thread.join();
    }
}
