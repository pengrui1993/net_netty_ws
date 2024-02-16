package com.love._api;

public interface Server extends QuitOperator {
    EventDispatcher dispatcher();
    UserOperator userOperator();
    NetOperator netOperator();
    boolean post(Runnable runnable);
    boolean debug = true;
}
