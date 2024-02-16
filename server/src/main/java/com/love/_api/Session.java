package com.love._api;

import io.netty.handler.codec.http.HttpHeaders;

public interface Session {
    Connection conn();
    boolean loginDone();
    RcvMsgReq getLastRequest();
    boolean isBlocked();
    void msgIdIncrement();
    void triggerBlockLogic(long now);
    void setLastRcvMsg(RcvMsgReq msg);


}
