package com.love._net;

import com.love._api.Connection;
import com.love._api.RcvMsgBody;
import com.love._api.RcvMsgHeader;
import com.love._api.RcvMsgReq;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._evt.NetMsgRcvEvent;
import com.love._evt.UserLogoutOkEvent;
import com.love._net.rcv.InstanceJoinRcvMsgBody;
import com.love._net.rcv.InstanceLeaveRcvMsgBody;
import com.love._net.send.LogoutResultBody;
import com.love._net.send.PongResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserMgrNetMsgHandler implements Listener {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final UserManager mgr;
    public UserMgrNetMsgHandler(UserManager userManager) {
        this.mgr = userManager;
    }
    @Override
    public void onEvent(Event evt) {
        NetMsgRcvEvent e = NetMsgRcvEvent.class.cast(evt);
        Connection conn = e.conn;
        RcvMsgReq req = e.msg;
        RcvMsgBody msg = req.body();
        final UserImpl user = mgr.userConnMgr.get(conn);
        switch (msg.type()){
            case LOGIN-> mgr.loginLogic.loginMessage(conn,req);
            case LOGOUT -> {
                if(!user.isLoginOk()){
                    logger.info("no login ignore,uid:"+user.uid);
                    conn.sendAndFlush(new LogoutResultBody(LogoutResultBody.NO_LOGIN));
                    return;
                }
                final UserCurLoginMgr uidUsers = mgr.userCurLoginMgr;
                final UserImpl remove = uidUsers.remove(user.uid);
                logger.info("{}",remove);
                user.doLogout();
                conn.sendAndFlush(new LogoutResultBody(LogoutResultBody.OK));
                mgr.server.dispatcher().emit(new UserLogoutOkEvent(conn,user.uid));
                logger.info("logout ok:"+user.uid);
            }
            case PING -> {
                conn.sendAndFlush(new PongResultBody());
            }
            case JOIN_INSTANCE -> {
                InstanceJoinRcvMsgBody join = InstanceJoinRcvMsgBody.class.cast(req.body());
                if(0!=user.currentInstanceId){
                    System.out.printf("warning: already exists instanceId:%d,will enter:%d\n"
                            ,user.currentInstanceId,join.instanceId);
                }
                user.currentInstanceId = join.instanceId;
            }
            case LEAVE_INSTANCE -> {
                InstanceLeaveRcvMsgBody leave = InstanceLeaveRcvMsgBody.class.cast(req.body());
                if(leave.instanceId!=user.currentInstanceId){
                    System.out.printf("warning: user existed room:%d,but leave:%d\n"
                            ,user.currentInstanceId,leave.instanceId);
                }
                user.currentInstanceId = 0;
            }
        }
    }
}
