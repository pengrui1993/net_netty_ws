package com.love._net.send;

import com.love._api.SendMsgBody;

public class ListInstanceResultBody implements SendMsgBody {
    @Override
    public Type type() {
        return Type.LIST_INSTANCE_RSP;
    }
}
