package com.love._net.send;

import com.love._api.SendMsgBody;

public class PongResultBody implements SendMsgBody {
    @Override
    public Type type() {
        return Type.PONG_RSP;
    }
}
