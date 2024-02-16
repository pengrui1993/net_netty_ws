package com.love._net.send;

import com.love._api.SendMsgBody;

public class DupLoginSameAccountBody implements SendMsgBody {
    @Override
    public Type type() {
        return Type.NOTIFY_ALREADY_LOGIN_OK_USER__OTHER_CONN_LOGIN_SAME_ACCOUNT_BUT_REJECT;
    }
    public final String msg = "已登录用户请知晓,其他地方有人正使用正确用户密码试图登录但是被拒绝，如果是密码泄露请修改密码";


}
