package com.love._api;

public interface SendMsgHeader {
    SendMsgHeader NULL = new SendMsgHeader(){};
    class Generic implements SendMsgHeader{
        public int cmd;
        public int mid;//message id
    }
}
