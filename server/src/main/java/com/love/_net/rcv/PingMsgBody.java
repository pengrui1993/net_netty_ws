package com.love._net.rcv;

public class PingMsgBody extends GenericRcvMsgBody {
    @Override
    public Type type() {
        return Type.PING;
    }
}
