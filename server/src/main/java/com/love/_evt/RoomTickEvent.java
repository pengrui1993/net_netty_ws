package com.love._evt;


public class RoomTickEvent extends SysTickEvent {
    @Override
    public RoomTickEvent reuse() {
        return (RoomTickEvent)super.reuse();
    }
}
