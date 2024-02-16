package com.love._evt;

import com.love._core.Event;

public class SysStartEvent implements Event {
    @Override
    public Type type() {
        return Type.SYS_START;
    }
}
