package com.love._api;

public interface CommandLine {
    enum Type{
        NO_IMPL,LIST_UID_USER,LIST_CONN_USER,USER_DEEP
    }
    interface Command{
        Type type();
    }
}
