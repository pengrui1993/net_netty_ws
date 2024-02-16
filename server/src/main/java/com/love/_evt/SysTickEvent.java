package com.love._evt;

import com.love._api.Api;
import com.love._core.Event;

public class SysTickEvent implements Event {
    @Override
    public Type type() {
        return Type.SYS_TICK;
    }
    long last;
    long delta;
    public SysTickEvent reuse(){
        long tmp = now();
        delta = tmp-last;
        last = tmp;
        return this;
    }
    public long getLast(){
        return last;
    }
    long now(){ return Api.now();}
}
