package com.love._evt;

import com.love._api.Session;
import com.love._core.Event;
import com.love._api.Connection;
import com.love._api.RcvMsgReq;

public class NetMsgRcvEvent implements Event {
    public final RcvMsgReq msg;
    public final Connection conn;
    public NetMsgRcvEvent(RcvMsgReq msg, Connection conn) {
        this.msg = msg;
        this.conn = conn;
        final Session session = conn.session();
        session.setLastRcvMsg(msg);
        session.msgIdIncrement();
    }

    @Override
    public Type type() {
        return Type.NET_MSG;
    }
}
