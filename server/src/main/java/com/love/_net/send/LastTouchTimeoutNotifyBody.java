package com.love._net.send;

import com.love._api.SendMsgBody;

public class LastTouchTimeoutNotifyBody implements SendMsgBody {
    public final long lastTouchTime;
    public final long serverClearTime;
    public LastTouchTimeoutNotifyBody(long lastTouchTime, long now) {
        this.lastTouchTime = lastTouchTime;
        this.serverClearTime = now;
    }
    @Override
    public Type type() {
        return Type.NOTIFY_LAST_TOUCH_TIMEOUT;
    }
}
