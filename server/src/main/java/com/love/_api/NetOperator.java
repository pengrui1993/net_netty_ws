package com.love._api;



public interface NetOperator {

    int INACTIVE_FLAG = 1
            ,DIFFERENCE_CONN_LOGIN_SAME_ACCOUNT = 2
            ;
    void onClose(Object ctx, int flag);
    Connection createConn(Object ctx);
    Connection getConn(Object ctx);
    boolean post(Runnable runnable);
    boolean usePost = false;
    boolean isHttpChannel(Object ctx);
    boolean isWsChannel(Object ctx);
    void addWsChannel(Object ctx);
    void removeWsChannel(Object ctx);
}
