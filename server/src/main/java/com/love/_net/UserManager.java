package com.love._net;

import com.love._api.*;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.*;
import com.love._stdin.UserDeep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * 连接
 *  * 登录
 *  * 登出
 * 关闭连接
 */
public class UserManager implements UserOperator {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Server server;
    final UserRepository userRepository = new UserRepository(this);
    final UserMgrLoginLogic loginLogic = new UserMgrLoginLogic(this);
    final UserConnMgr userConnMgr = new UserConnMgr(this);
    final UserCurLoginMgr userCurLoginMgr = new UserCurLoginMgr(this);
    final UserHistoryMgr histories = new UserHistoryMgr(this);
    final Listener start = evt->init();
    final Listener tick = evt->onTick(SysTickEvent.class.cast(evt));
    final Listener stop = evt->destroy();
    final Listener connHandler = evt -> userConnMgr.onConnected(ConnectedEvent.class.cast(evt).conn);
    final Listener disConn = evt -> userConnMgr.onDisconnected(DisconnectedEvent.class.cast(evt).conn);
    final UserMgrNetMsgHandler netMsgHandler = new UserMgrNetMsgHandler(this);
    final Listener cmdline = evt->{
        final CmdLineMsgRcvEvent cle = CmdLineMsgRcvEvent.class.cast(evt);
        final CommandLine.Command cmd = cle.cmd;
        switch (cmd.type()){
            case LIST_UID_USER -> userCurLoginMgr.listAllUser();
            case LIST_CONN_USER -> userConnMgr.listUser();
            case USER_DEEP->{
                final UserDeep d = UserDeep.class.cast(cmd);
                userCurLoginMgr.listDeepUser(d.getId(),d.out);
            }
        }
    };
    public UserManager(Server server) {
        this.server = server;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.SYS_START,start);
        dispatcher.on(Event.Type.SYS_STOP,stop);
    }

    void onTick(SysTickEvent e){
        userCurLoginMgr.onTick(e);
        histories.onTick(e);
        userConnMgr.onTick(e);
    }
    void print(Object o){
        System.out.println(o);
    }
    boolean working;
    void init(){
        if(working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.CONN, connHandler);
        dispatcher.on(Event.Type.SYS_TICK,tick);
        dispatcher.on(Event.Type.DIS_CONN,disConn);
        dispatcher.on(Event.Type.NET_MSG, netMsgHandler);
        dispatcher.on(Event.Type.CMD_LINE_MSG, cmdline);
        working = true;
    }
    void destroy(){
        if(!working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.off(Event.Type.CONN, connHandler);
        dispatcher.off(Event.Type.SYS_TICK,tick);
        dispatcher.off(Event.Type.DIS_CONN,disConn);
        dispatcher.off(Event.Type.NET_MSG, netMsgHandler);
        dispatcher.off(Event.Type.CMD_LINE_MSG, cmdline);
        dispatcher.off(Event.Type.SYS_START,start);
        dispatcher.off(Event.Type.SYS_STOP,stop);
        logger.info("need to be destroy");
    }

    @Override
    public UserInfo get(int user) {
        return userCurLoginMgr.get(user);
    }
}
class CheckLogin{
    String uname;
    String pwd;
    CheckLogin reuse(String u,String p){
        uname = u;
        pwd = p;
        return this;
    }
}