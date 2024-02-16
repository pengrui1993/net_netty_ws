package com.love._evt;

import com.love._core.Event;
import com.love._api.Connection;

public class UserLoginOkEvent implements Event {
    public final int uid;
    public final Connection conn;
    public static final int STATE_NORMAL = 1
            ,LOGOUT_CONN_EXISTS = 2;
    public final int status;
    public UserLoginOkEvent(Connection conn, int uid) {
        this(conn, uid,STATE_NORMAL);
    }
    public UserLoginOkEvent(Connection conn, int uid, int state){
        this.conn = conn;
        this.uid = uid;
        this.status = state;
    }

    @Override
    public Type type() {
        return Type.USER_LOGIN_OK;
    }
}
