package com.love._evt;

import com.love._api.QuitOperator;
import com.love._core.Event;
public class SysQuitEvent implements Event {
    public final QuitOperator who;
    public SysQuitEvent(QuitOperator who) {
        this.who = who;
    }
    @Override
    public Type type() {
        return Type.SYS_QUIT_REQ;
    }
}