package com.love._logic;

import com.love._core.Actor;
import com.love._core.Room;

import java.util.Map;

public class ActorManager {
    private Map<Long, Actor> actors;
    final Room room;
    public ActorManager(Room room) {
        this.room = room;
    }
}
