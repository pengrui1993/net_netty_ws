package com.love._net.rcv;


public class InstanceCreateRcvMsgBody extends GenericRcvMsgBody {
    @Override
    public Type type() {
        return Type.CREATE_INSTANCE;
    }
}
