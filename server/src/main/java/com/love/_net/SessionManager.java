package com.love._net;

import com.love._api.EventDispatcher;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.*;
import com.love._api.Connection;
import com.love._api.NetOperator;
import com.love._api.SessionOperator;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SessionManager implements SessionOperator {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Server server;
    final Listener start  = (evt)-> init();
    final Listener stop  = (evt)->destroy();
    final Listener tick  = (evt)->{onTick(SysTickEvent.class.cast(evt));};
    final Listener loginOkHandler = (evt)-> onLoginOk(UserLoginOkEvent.class.cast(evt));
    final Listener logoutOkHandler = (evt)-> onLogoutOk(UserLogoutOkEvent.class.cast(evt));
    void onLogoutOk(UserLogoutOkEvent e){
        SessionImpl session = SessionImpl.class.cast(e.conn);
        if(!session.loginDone()){
            logger.info("warning:no login");
        }
        session.onUserLogoutOk(e.uid);
    }
    void onLoginOk(UserLoginOkEvent e){
        SessionImpl session = SessionImpl.class.cast(e.conn);
        if(!sessions.containsKey(session.fd())){
            logger.info("warning: session manager conn err");
            return;
        }
        if(session.loginDone()){
            logger.info("warning,duplicated login");
        }
        session.onUserLoginOk(e.uid);
    }
    void init(){
        if(working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.SYS_TICK,tick);
        dispatcher.on(Event.Type.USER_LOGIN_OK, loginOkHandler);
        dispatcher.on(Event.Type.USER_LOGOUT_OK, logoutOkHandler);
        working = true;
    }
    void destroy(){
        if(!working)return;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.off(Event.Type.SYS_TICK,tick);
        dispatcher.off(Event.Type.USER_LOGIN_OK, loginOkHandler);
        dispatcher.off(Event.Type.USER_LOGOUT_OK, logoutOkHandler);
        dispatcher.off(Event.Type.SYS_START,start);
        dispatcher.off(Event.Type.SYS_STOP,stop);
        logger.info("need to be destroy");
    }
    boolean working;
    public SessionManager(Server server, NetOperator netManager) {
        this.server = server;
        final EventDispatcher dispatcher = server.dispatcher();
        dispatcher.on(Event.Type.SYS_START,start);
        dispatcher.on(Event.Type.SYS_STOP,stop);
        working = false;
    }
    //key:ChannelHandlerContext
    final Map<Object, SessionImpl> sessions = new HashMap<>();
    @Override
    public Connection getConn(Object ctx) {
        return sessions.get(ctx);
    }
    @Override
    public Connection netCreate(Object ctx) {
        final ChannelHandlerContext fd = (ChannelHandlerContext)ctx;
        final SessionImpl session = new SessionImpl(fd, server);
        sessions.put(fd,session);
        return session;
    }
    @Override
    public void netClose(Object ctx,int flag) {
        SessionImpl session = sessions.get(ctx);
        switch(flag){
            case NetOperator.DIFFERENCE_CONN_LOGIN_SAME_ACCOUNT -> session.ctx.close();
            case NetOperator.INACTIVE_FLAG -> {
                sessions.remove(ctx);
                server.post(()-> server.dispatcher().emit(new DisconnectedEvent(session)));
            }
            default -> throw new UnsupportedOperationException();
        }
    }
    final List<SessionImpl> tickList =new LinkedList<>();
    final long kickDuration = 1*60*1000;
    void onTick(SysTickEvent e) {
        long now = e.getLast();
        tickList.clear();
        tickList.addAll(sessions.values());
        for (SessionImpl session : tickList) {
            if(now-session.lastActiveTime>kickDuration){
                session.kicked();
            }
        }
        for (SessionImpl value : sessions.values()) {
            value.blockLogicTick(now);
        }
    }
}
