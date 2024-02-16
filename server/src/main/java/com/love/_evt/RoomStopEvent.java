package com.love._evt;


import com.love._core.Event;

public class RoomStopEvent implements Event {
    @Override
    public Type type() {
        return Type.ROOM_STOP;
    }
}
