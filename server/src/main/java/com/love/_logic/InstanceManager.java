package com.love._logic;

import com.love._api.*;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.*;
import com.love._net.send.ListInstanceResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class InstanceManager implements InstanceOperator {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final LinkedHashMap<Integer, Instance> instances = new LinkedHashMap<>();
    final Server server;
    int idGen = 0;
    public InstanceManager(Server server) {
        this.server = server;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.SYS_START,start);
        dispatcher.on(Event.Type.SYS_STOP,stop);
    }
    final Listener start = evt->init();
    final Listener tick = evt->onTick(SysTickEvent.class.cast(evt));
    final Listener stop = evt->destroy();
    final Listener conn = evt -> onConnected(ConnectedEvent.class.cast(evt).conn);
    final Listener disConn = evt -> onDisconnected(DisconnectedEvent.class.cast(evt).conn);
    final Listener netMsgHandler = evt->{
        NetMsgRcvEvent e = NetMsgRcvEvent.class.cast(evt);
        onMessage(e.conn,e.msg);
    };
    void onConnected(Connection conn){}
    void onDisconnected(Connection conn){}
    final ArrayList<Instance> tickList = new ArrayList<>();
    void onTick(SysTickEvent e){
//        logger.info("instance mgr tick:{}",e.getLast());
        tickList.clear();
        tickList.addAll(instances.values());
        for (Instance instance : tickList) {
            if(instance.isFinish()){
                instance.clear();
                instances.remove(instance.getId());
            }
        }
    }
    private List<InstanceInfo> pageInfo(int page,int size){
        final int total = instances.size();
        if(total==0)return Collections.emptyList();
        final int first = page*size+(size-1);
        if(first>=total)return Collections.emptyList();
        final ArrayList<Instance> arr = new ArrayList<>(instances.values());
        final List<InstanceInfo> result = new LinkedList<>();
        for(int i = first;i<first+size;i++){
            if(i>=total)return result;
            result.add(arr.get(i).getInfo());
        }
        return result;
    }
    void onMessage(Connection conn, RcvMsgReq msg){
        RcvMsgBody body = msg.body();
        RcvMsgHeader header = msg.header();
        switch (body.type()){
            case LIST_INSTANCE -> {
                conn.sendAndFlush(new ListInstanceResultBody());
            }
            case CREATE_INSTANCE-> {
                if(!msgCreateInstance(conn,msg)){
                    logger.info("create instance failure");
                }
            }
            case JOIN_INSTANCE -> {
                logger.info("join");
            }
            case LEAVE_INSTANCE -> {
                logger.info("leave");
            }
            case HELLO -> {
                logger.info("hello:{}",msg.body());
            }
        }
    }
    static final int maxInstance = 1000000;
    boolean msgCreateInstance(Connection conn, RcvMsgReq msg){
        long size = instances.size();
        if(size>=maxInstance){
            logger.info("room");
            return false;
        }
        while(instances.containsKey(++idGen));
        instances.put(idGen,new Instance(idGen,"",server));
        return true;
    }
    boolean working;
    void init(){
        if(working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.CONN,conn);
        dispatcher.on(Event.Type.SYS_TICK,tick);
        dispatcher.on(Event.Type.DIS_CONN,disConn);
        dispatcher.on(Event.Type.NET_MSG, netMsgHandler);
        working = true;
    }
    void destroy(){
        if(!working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.off(Event.Type.CONN,conn);
        dispatcher.off(Event.Type.SYS_TICK,tick);
        dispatcher.off(Event.Type.DIS_CONN,disConn);
        dispatcher.off(Event.Type.NET_MSG, netMsgHandler);
        dispatcher.off(Event.Type.SYS_START,start);
        dispatcher.off(Event.Type.SYS_STOP,stop);
        logger.info("need to be destroy");
    }
}
