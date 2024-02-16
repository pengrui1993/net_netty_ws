package com.love._evt;

import com.love._core.Event;
import com.love._api.Connection;

public class ConnectedEvent implements Event {
    public final Connection conn;
    public ConnectedEvent(Connection conn) {
        this.conn = conn;
    }

    @Override
    public Type type() {
        return Type.CONN;
    }
}
