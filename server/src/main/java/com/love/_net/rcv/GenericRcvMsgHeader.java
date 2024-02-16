package com.love._net.rcv;

import com.love._api.RcvMsgHeader;

public class GenericRcvMsgHeader implements RcvMsgHeader {
    public int cmd;//command id;
    public int mid;//message id to answer
    public Integer uid;//user id
    public int sid;//sequence id
    public long tm;//client send message timestamp
    public final long serverCreateTime = now();
    @Override
    public long serverMsgTime() {
        return serverCreateTime;
    }
    @Override
    public long clientMsgTIme() {
        return tm;
    }
    @Override
    public int sequenceId() {
        return sid;
    }
    @Override
    public int cmd() {
        return cmd;
    }
    @Override
    public int requestAnswerMatchId() {
        return mid;
    }

}
