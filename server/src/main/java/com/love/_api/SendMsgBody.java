package com.love._api;

/**
 * server to client
 */
public interface SendMsgBody {
    enum Type{
        CONN_PREPARING,PONG_RSP,LIST_INSTANCE_RSP, LOGIN_RESULT_RSP,LOGOUT_RESULT_RSP
        , NOTIFY_ALREADY_LOGIN_OK_USER__OTHER_CONN_LOGIN_SAME_ACCOUNT_BUT_REJECT
        ,NOTIFY_LAST_TOUCH_TIMEOUT
    }
    Type type();
}