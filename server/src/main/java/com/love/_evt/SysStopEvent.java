package com.love._evt;

import com.love._core.Event;

public class SysStopEvent implements Event {
    @Override
    public Type type() {
        return Type.SYS_STOP;
    }
}
