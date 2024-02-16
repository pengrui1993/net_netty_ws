package com.love._evt;

import com.love._core.Event;
import com.love._api.Connection;
import com.love._api.UserInfo;

public class UserLogoutOkEvent implements Event {
    public final int uid;
    public final Connection conn;
    public UserLogoutOkEvent(Connection conn, int uid) {
        this.uid = uid;
        this.conn = conn;
    }

    @Override
    public Type type() {
        return Type.USER_LOGOUT_OK;
    }
}
