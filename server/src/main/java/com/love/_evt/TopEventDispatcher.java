package com.love._evt;

import com.love._api.EventDispatcher;
import com.love._core.Event;
import com.love._core.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TopEventDispatcher implements EventDispatcher {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    enum State{
        INIT,START_FOREACH,END_FOREACH
    }
    volatile State state = State.INIT;
    final Map<Event.Type, List<Listener>> ctx = new HashMap<>();
    final Set<Event.Type> dirty = new HashSet<>();
    List<Listener> newList(){
//        new CopyOnWriteArrayList<>()
        return new LinkedList<>();
    }
    Map<Event.Type, List<Listener>> onTmp = new HashMap<>();
    /*
    java.util.ConcurrentModificationException
	at java.base/java.util.LinkedList$ListItr.checkForComodification(LinkedList.java:970)
	at java.base/java.util.LinkedList$ListItr.next(LinkedList.java:892)
	at com.example._evt.TopEventDispatcher.emit(TopEventDispatcher.java:58)
	at com.example.GameServer.start(GameServer.java:31)
	at com.example.Starter.main(Starter.java:5)
     */
    void concurrentLogic(){
        if(onTmp.isEmpty()&&offTmp.isEmpty())return;
        logger.info("TopEventDispatcher.concurrentLogic");
        if(!onTmp.isEmpty()){
            for (Map.Entry<Event.Type, List<Listener>> e : onTmp.entrySet()) {
                Event.Type type = e.getKey();
                List<Listener> listeners = ctx.get(type);
                if(Objects.isNull(listeners)){
                    listeners = newList();
                    ctx.put(type,listeners);
                }
                List<Listener> value = e.getValue();
                listeners.addAll(value);
                dirty.add(type);
            }
            onTmp.clear();
        }
        if(!offTmp.isEmpty()){
            for (Map.Entry<Event.Type, List<Listener>> e : offTmp.entrySet()) {
                Event.Type type = e.getKey();
                List<Listener> listeners = ctx.get(type);
                if(Objects.isNull(listeners)){
                    logger.info("warning,concurrency off found empty working listener");
                    dirty.remove(type);
                    continue;
                }
                List<Listener> value = e.getValue();
                listeners.removeAll(value);
                if(listeners.isEmpty()){
                    ctx.remove(type);
                }
                dirty.remove(type);
            }
            offTmp.clear();
        }
    }
    @Override
    public void on(Event.Type type, Listener listener) {
        if(Thread.currentThread()!=main)throw new UnsupportedOperationException("must be in main thread");
        if(Objects.isNull(type)||Objects.isNull(listener))return;
        List<Listener> listeners = ctx.get(type);
        if(Objects.isNull(listeners)){
            listeners = newList();
            ctx.put(type,listeners);
        }
        if(State.START_FOREACH==state){
            List<Listener> tmp = onTmp.get(type);
            if(Objects.isNull(tmp)){
                tmp = new LinkedList<>();
                onTmp.put(type,tmp);
            }
            tmp.add(listener);
        }else{
            listeners.add(listener);
            //        listeners.sort(Comparator.comparingInt(Listener::order));
            dirty.add(type);
        }
    }
    final Thread main;
    public TopEventDispatcher(){
        main = Thread.currentThread();
    }

    final Map<Event.Type,List<Listener>> offTmp = new HashMap<>();
    @Override
    public void off(Event.Type type, Listener listener) {
        if(Objects.isNull(type)||Objects.isNull(listener))return;
        List<Listener> listeners = ctx.get(type);
        if(Objects.isNull(listeners))return;
        if(State.START_FOREACH==state){
            List<Listener> tmp = offTmp.get(type);
            if(Objects.isNull(tmp)){
                tmp = newList();
                offTmp.put(type,tmp);
            }
            tmp.add(listener);
        }else{
            listeners.remove(listener);
            if(listeners.isEmpty())ctx.remove(type);
            dirty.add(type);
        }
    }
    @Override
    public void emit(Event evt) {
        concurrentLogic();
        if(Objects.isNull(evt)||Objects.isNull(evt.type())){
            logger.info("null params:"+evt);
            return;
        }
        final List<Listener> listeners = ctx.get(evt.type());
        if(Objects.isNull(listeners)){
            logger.info("empty listener for type:"+evt.type());
            return;
        }
        if(dirty.contains(evt.type())){
            listeners.sort(Comparator.comparingInt(Listener::order));
            dirty.remove(evt.type());
        }
        state = State.START_FOREACH;
        for (Listener listener : listeners) {
            listener.onEvent(evt);
        }
        state = State.END_FOREACH;
    }

    @Override
    public void channels(Map<Event.Type, List<Listener>> container) {
        if(Objects.isNull(container))return;
        container.putAll(this.ctx);
    }

    @Override
    public void onTemp(Map<Event.Type, List<Listener>> container) {
        if(Objects.isNull(container))return;
        container.putAll(onTmp);
    }

    @Override
    public void offTemp(Map<Event.Type, List<Listener>> container) {
        if(Objects.isNull(container))return;
        container.putAll(offTmp);
    }

    @Override
    public boolean isTop() {
        return TopEventDispatcher.class==this.getClass();
    }
}
