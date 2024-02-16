package com.love._api;

public interface Protocol {
    interface Encoder<OUT>{
        OUT encode(SendMsgRsp msg);
    }
    interface Decoder<IN>{
        RcvMsgReq decode(IN msg);
    }
}
