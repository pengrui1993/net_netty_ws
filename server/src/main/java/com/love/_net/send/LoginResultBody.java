package com.love._net.send;

import com.love._api.SendMsgBody;

public class LoginResultBody implements SendMsgBody {
    public static final int NO_ACCOUNT = -1
            ,PWD_ERR = -2
            ,ACCOUNT_LOCKED = -3
            ,ACCOUNT_LOCKED_NO_TRY = -4
            ,DIFFERENCE_CONN_LOGIN_SAME_ACCOUNT = -5
            ,OK = 1
            ,SAME_CONN_DUPLICATE_LOGIN = 2
            ;

    public final String msg;
    public final int result;
    public final int instanceId;
    public final int token;
    public LoginResultBody(int loginResult, int instanceId, int token) {
        this(loginResult,instanceId,token,null);
    }
    public LoginResultBody(int loginResult, int instanceId, int token,String msg) {
        this.result = loginResult;
        this.instanceId = instanceId;
        this.token = token;
        this.msg = msg;
    }
    @Override
    public Type type() {
        return Type.LOGIN_RESULT_RSP;
    }
}
