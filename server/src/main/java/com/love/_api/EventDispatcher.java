package com.love._api;

import com.love._core.Event;
import com.love._core.Listener;

import java.util.List;
import java.util.Map;

public interface EventDispatcher {
    void on(Event.Type type, Listener listener);
    void off(Event.Type type,Listener listener);
    void emit(Event evt);
    void channels(Map<Event.Type, List<Listener>> container);
    void onTemp(Map<Event.Type, List<Listener>> container);
    void offTemp(Map<Event.Type, List<Listener>> container);
    boolean isTop();
}
