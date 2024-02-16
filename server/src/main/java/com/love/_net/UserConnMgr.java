package com.love._net;

import com.love._api.Connection;
import com.love._evt.SysTickEvent;
import com.love._net.send.LastTouchTimeoutNotifyBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class UserConnMgr {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Map<Connection,UserImpl> users = new HashMap<>();
    final UserManager mgr;
    public UserConnMgr(UserManager userManager) {
        this.mgr = userManager;
    }
    void onConnected(Connection conn){
        users.put(conn,new UserImpl(conn));
    }
    void onDisconnected(Connection conn){
        UserImpl user = users.get(conn);
        if(Objects.isNull(user)){
            logger.info("warning, disconnecting action is missing");
            return;
        }
        System.out.println("UserConnMgr.onDisconnected");
        user.conn=null;
        users.remove(conn);
    }
    UserImpl get(Connection conn) {
        return users.get(conn);
    }
    void replace(Connection conn, UserImpl user) {
        if(Objects.isNull(user)){
            logger.warn("try to replace null user to conn , ignore that");
            return;
        }
        final UserImpl old = users.get(conn);
        if(Objects.isNull(old)){
            logger.error("no contain,conn:{},user:{}",conn,user);
        }
        users.put(conn,user);
        logger.info("replace conn's user from {} to {}",old,user);
    }
    void listUser() {
        logger.info(users.size()+" connections\n"+ users);
    }
    void onTouchTimeout(UserImpl user,long now) {
        logger.info("timeout of last touch time,last touch time:{},now:{} clear uid:{}"
                ,user.lastTouchTime,now,user.uid);
        users.put(user.conn,new UserImpl(user));
        user.conn.sendAndFlush(new LastTouchTimeoutNotifyBody(user.lastTouchTime,now));
    }

    void onTick(SysTickEvent e) {
    }


}
