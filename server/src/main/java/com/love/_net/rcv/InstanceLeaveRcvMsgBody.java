package com.love._net.rcv;


public class InstanceLeaveRcvMsgBody extends GenericRcvMsgBody {
    public int instanceId;
    @Override
    public Type type() {
        return Type.LEAVE_INSTANCE;
    }
}
