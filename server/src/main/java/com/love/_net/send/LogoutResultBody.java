package com.love._net.send;

import com.love._api.SendMsgBody;

public class LogoutResultBody implements SendMsgBody {
    public static final int
            NO_LOGIN = -1
            ,OK = 1
            ;
    static final String okMsg ="退出登录ok";
    static final String noLoginMsg ="还未登录";
    public LogoutResultBody(int state) {
        this.state = state;
        msg = state == OK?okMsg:noLoginMsg;
    }

    @Override
    public Type type() {
        return Type.LOGOUT_RESULT_RSP;
    }
    public final int state;
    public final String msg;
}
