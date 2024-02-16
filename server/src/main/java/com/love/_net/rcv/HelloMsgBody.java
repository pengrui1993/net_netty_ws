package com.love._net.rcv;


import com.love._util.JsonUtil;

public class HelloMsgBody extends GenericRcvMsgBody {
    @Override
    public Type type() {
        return Type.HELLO;
    }
    public final String info = "just say hello";
    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }
}
