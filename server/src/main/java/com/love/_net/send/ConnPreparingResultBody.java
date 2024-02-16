package com.love._net.send;

import com.love._api.SendMsgBody;

public class ConnPreparingResultBody implements SendMsgBody {
    public static final ConnPreparingResultBody INSTANCE = new ConnPreparingResultBody();
    private ConnPreparingResultBody(){}
    @Override
    public Type type() {
        return Type.CONN_PREPARING;
    }
}
