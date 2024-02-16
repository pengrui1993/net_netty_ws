package com.love._net;

import com.love._anno.NoRepo;
import com.love._api.Connection;
import com.love._api.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

class UserImpl implements UserInfo {
    static final Logger logger = LoggerFactory.getLogger(UserImpl.class);
    void deepPrint(PrintStream out) {
        UserImpl ref = this;
        int deep = 0;
        List<UserImpl> list = new LinkedList<>();
        while(Objects.nonNull(ref)){
            deep++;
            list.add(ref);
            ref = ref.old;
        }
        out.printf("deep:%d,list:%s\n",deep,list);
    }
    //logger.info
    enum State{
        CONN,LOGIN_OK,LOGOUT_OK
    }
    State state;
    Connection conn;
    int uid;
    int token;
    @NoRepo
    int currentInstanceId;//room id
    @NoRepo
    long lastTouchTime;//to check ping pong


    boolean isLoginOk(){
        return state == State.LOGIN_OK;
    }
    void doLoginOk(int uid) {
        if(state==State.LOGIN_OK)logger.info("warning,already login_ok");
        this.uid = uid;
        state = State.LOGIN_OK;
    }
    void doLogout(){
        if(!isLoginOk())logger.info("warning,should be login_ok to logout_ok");
        if(0!=currentInstanceId) logger.info("warning ,logout must be leaved room");
        uid = 0;
        state = State.LOGOUT_OK;
    }
    UserImpl(Connection conn){
        this.conn = conn;
        state = State.CONN;
        token = ThreadLocalRandom.current().nextInt();
        lastTouchTime = now();
    }
    UserImpl(UserImpl touchTimeoutUser){
        this(touchTimeoutUser.conn);
        old = touchTimeoutUser;
    }
    UserImpl old;
    @Override
    public int id() {
        return uid;
    }

    @Override
    public String toString() {
        return "UserImpl{" +
                "state=" + state +
                ", uid=" + uid +
                ", token=" + token +
                '}';
    }
}