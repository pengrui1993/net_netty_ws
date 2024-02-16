package com.love._net.rcv;


public class InstanceJoinRcvMsgBody extends GenericRcvMsgBody {
    public int instanceId;
    @Override
    public Type type() {
        return Type.JOIN_INSTANCE;
    }
}
