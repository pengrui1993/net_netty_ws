package com.love._net.send;

import com.love._api.RcvMsgHeader;
import com.love._api.SendMsgHeader;

public class RspMsgHeader extends SendMsgHeader.Generic{
    public Integer token;
    public Integer rid;//room id,just instance id
    public RspMsgHeader(RcvMsgHeader requestHeader) {
        this.cmd = requestHeader.cmd();
        this.mid = requestHeader.requestAnswerMatchId();
    }
}
