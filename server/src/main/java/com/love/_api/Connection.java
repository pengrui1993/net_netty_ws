package com.love._api;

public interface Connection {
    Object fd();
    void sendAndFlush(SendMsgBody msg);
    Session session();
    default long now(){ return Api.now();}

}
