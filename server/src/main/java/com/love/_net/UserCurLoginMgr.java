package com.love._net;

import com.love._evt.SysTickEvent;
import com.love._util.ClockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.*;

class UserCurLoginMgr {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Map<Integer,UserImpl> uidUsers = new HashMap<>();//logout 和 tick 才会清除该缓存
    final UserManager mgr;
    UserCurLoginMgr(UserManager userManager) {
        this.mgr = userManager;
    }
    private final List<UserImpl> tickList = new LinkedList<>();
    boolean stop = true;
    final ClockRunner runner = new ClockRunner(()-> logger.info("ignore tick now, please implement that logic"),100000);
    void onTick(SysTickEvent e) {
            if(stop){
                runner.tick();
                return;
            }
            tickList.clear();
            tickList.addAll(uidUsers.values());
            long duration = 60*1000;
            long now = e.getLast();
            for (UserImpl user : tickList) {
                if(now-user.lastTouchTime>=duration){
                    uidUsers.remove(user.uid);
                    mgr.userConnMgr.onTouchTimeout(user,now);
                }
            }
    }
    UserImpl get(int uid) {
        return uidUsers.get(uid);
    }
    UserImpl getAndTouch(int uid,long now) {
        final UserImpl user = uidUsers.get(uid);
        if(Objects.nonNull(user)){
            user.lastTouchTime = now;
        }
        return user;
    }
    UserImpl put(int uid, UserImpl user) {
        if(uid==0)return null;
        return uidUsers.put(uid,user);
    }
    UserImpl remove(int uid) {
        return uidUsers.remove(uid);
    }
    void listAllUser() {
        logger.info(uidUsers.toString());
    }
    void listDeepUser(int uid, PrintStream out){
        final UserImpl userInfo = uidUsers.get(uid);
        if(Objects.isNull(userInfo)){
            logger.warn("user info is not that impl");
            return;
        }
        userInfo.deepPrint(out);
    }
}
