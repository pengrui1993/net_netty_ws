package com.love._evt;

import com.love._core.Event;
import com.love._api.Connection;

public class DisconnectedEvent implements Event {
    public final Connection conn;
    public DisconnectedEvent(Connection conn) {
        this.conn = conn;
    }
    @Override
    public Type type() {
        return Type.DIS_CONN;
    }
}
