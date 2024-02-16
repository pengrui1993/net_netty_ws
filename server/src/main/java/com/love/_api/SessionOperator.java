package com.love._api;

public interface SessionOperator {
    Connection netCreate(Object ctx);
    void netClose(Object ctx,int flag);
    Connection getConn(Object ctx);
}
