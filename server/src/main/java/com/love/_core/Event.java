package com.love._core;

public interface Event {
    enum Type{
        SYS_NULL,SYS_START,SYS_TICK,SYS_QUIT_REQ,SYS_STOP,CONN,DIS_CONN,NET_MSG,CMD_LINE_MSG

        ,USER_LOGIN_OK,USER_LOGOUT_OK
        ,ROOM_START,ROOM_STOP,ROOM_TICK
    }

    Type type();
}
