package com.love._net;

import com.love._api.*;
import com.love._evt.UserLoginOkEvent;
import com.love._net.rcv.LoginMsgBody;
import com.love._net.send.DupLoginSameAccountBody;
import com.love._net.send.LoginResultBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.Consumer;

import static com.love._net.send.LoginResultBody.*;

class UserMgrLoginLogic {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final boolean loginSameAccountForceQuit = false;
    final UserManager mgr;
    public UserMgrLoginLogic(UserManager userManager) {
        mgr = userManager;
    }

    /**
     * 单连接测试：1 2 3 4 5 6
     * 双连接测试：7，8
     *         //2,6,1//no account ,login ok, duplicated login
     *         //5,4,3//err ,locked,locked no try
     *         //6,7//difference connection login ok same account when other no logout and no disconnected
     *         //6,8//one connection login then no logout but disconnected , other connection create , then du that
     */
    void loginMessage(Connection conn, RcvMsgReq req){
        final long start = Api.now();
        final UserImpl user = mgr.userConnMgr.get(conn);
        if(user.isLoginOk()){
            conn.sendAndFlush(new LoginResultBody(SAME_CONN_DUPLICATE_LOGIN,0,0));
            logger.info("same connect duplicate login");
            return;
        }//1
        final LoginMsgBody login = LoginMsgBody.class.cast(req.body());
        mgr.userRepository.getUid(login.uname, uid -> mgr.server.post(()->{
            if(0==uid){//no account,ddos attach!!!!
                conn.session().triggerBlockLogic(req.header().serverMsgTime());
                logger.info("uname not exists:"+login.uname);
                conn.sendAndFlush(new LoginResultBody(NO_ACCOUNT,0,0));
                return;
            }//2
            final UserHistory history = mgr.histories.requireHistory(uid);
            if(history.isLocked()){
                conn.sendAndFlush(new LoginResultBody(ACCOUNT_LOCKED_NO_TRY,0,0));
                logger.info("locked no try,uid:"+uid);
                return;
            }//3
            final RcvMsgHeader header = req.header();
            final CheckLogin reuse = checkLogin.reuse(login.uname, login.pwd);
            mgr.userRepository.checkPermission(reuse, unamePwdOk -> mgr.server.post(()->{
                if(!unamePwdOk){//no matched uname ,pwd
                    int limit = 3;
                    final Runnable limited = ()->{
                        conn.sendAndFlush(new LoginResultBody(ACCOUNT_LOCKED,0,0));
                        logger.info("err pwd and locked");
                    };//4
                    final Runnable justPwdErr = ()->{
                        logger.info("err pwd,times:"+history.errPwdWhenLoginCounter);
                        conn.sendAndFlush(new LoginResultBody(PWD_ERR,0,0));
                    };//5
                    history.loginFailure(limit,header.serverMsgTime());
                    final Runnable info = history.isLocked()?limited:justPwdErr;
                    info.run();
                    return;
                }
                history.loginSuccess(header.serverMsgTime());
                final UserCurLoginMgr uidUsers = mgr.userCurLoginMgr;
                final UserImpl existedUser = uidUsers.get(uid);
                final Server server = mgr.server;
                if(Objects.isNull(existedUser)){//normal login ok
                    user.doLoginOk(uid);
                    uidUsers.put(uid,user);
                    server.dispatcher().emit(new UserLoginOkEvent(conn,uid));
                    logger.info("normal duration:{}",Api.now()-start);
                    conn.sendAndFlush(new LoginResultBody(OK,0,user.token));
                    logger.info("login ok\nuid:{},all:{}",uid, uidUsers.uidUsers);
                    return;
                }//6
                if(Objects.nonNull(existedUser.conn)){//pre conn login and exception but no clear the user info ,user reconnect that;
                    logger.info("difference conn login same account");
                    final String msg = "您所使用的用户密码已经在其他地方登录，请稍后再试";
                    conn.sendAndFlush(new LoginResultBody(DIFFERENCE_CONN_LOGIN_SAME_ACCOUNT,0,0,msg));
                    existedUser.conn.sendAndFlush(new DupLoginSameAccountBody());
                    if(loginSameAccountForceQuit){
                        server.post(()->server.netOperator().onClose(conn.fd(), NetOperator.DIFFERENCE_CONN_LOGIN_SAME_ACCOUNT));
                    }
                    return;
                }//7
                //now login ok ,but cached user info,because the uid user no logout before but disconnected
                existedUser.conn = conn;
                mgr.userConnMgr.replace(conn,existedUser);
                logger.info("re conn duration:{}",Api.now()-start);
                logger.info("reconnected user login flag:"+existedUser.uid);//user.loginOk = true;//should be true
                server.dispatcher().emit(new UserLoginOkEvent(conn,existedUser.uid,UserLoginOkEvent.LOGOUT_CONN_EXISTS));
                conn.sendAndFlush(new LoginResultBody(OK,existedUser.currentInstanceId,existedUser.token));
                //8
            }));
        }));
    }
    //bug:same client do that, 1 conn 2 login 3 disconn 4 conn 5 login
    final CheckLogin checkLogin = new CheckLogin();

}
