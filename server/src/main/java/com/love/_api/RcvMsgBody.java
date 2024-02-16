package com.love._api;


public interface RcvMsgBody {
    enum Type{
        HELLO
        ,LOGIN,LOGOUT
        ,PING
        ,LIST_INSTANCE,CREATE_INSTANCE
        ,JOIN_INSTANCE,LEAVE_INSTANCE
    }
    Type type();

    default long now(){ return Api.now();}
}