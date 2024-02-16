package com.love._net.rcv;


public class LogoutMsgBody extends GenericRcvMsgBody {
    @Override
    public Type type() {
        return Type.LOGOUT;
    }
}
