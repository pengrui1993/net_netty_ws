package com.love._net.rcv;


public class LoginMsgBody extends GenericRcvMsgBody {
    public String uname;
    public String pwd;
    @Override
    public Type type() {
        return Type.LOGIN;
    }
    @Override
    public String toString() {
        return "LoginMsgBody{" +
                "uname='" + uname + '\'' +
                ", pwd='" + pwd + '\'' +
                '}';
    }
}
