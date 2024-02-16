package com.love._net;

import com.love._evt.SysTickEvent;
import com.love._util.ClockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

class UserHistoryMgr {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Map<Integer, UserHistory> history= new HashMap<>();
    final UserManager mgr;
    UserHistoryMgr(UserManager userManager) {
        mgr = userManager;
    }
    List<UserHistory> hisTickList = new LinkedList<>();
    final boolean stop = true;
    final ClockRunner runner = new ClockRunner(()-> logger.info("account lock supporter history ignore tick now, please implement that logic"),100000);
    void onTick(SysTickEvent e) {
        if(stop){
            runner.tick();
            return;
        }
        hisTickList.clear();
        hisTickList.addAll(this.history.values());
        long duration =10*1000;
        long now = e.getLast();
        for (UserHistory his : hisTickList) {
            if(-1L ==his.lastLoginFailureTime)continue;
            if(now-his.lastLoginFailureTime>duration){
                logger.info("pre reset lock:{}",his);
                his.resetLock();
                logger.info("post reset lock:{}",his);
            }
        }
}
    UserHistory requireHistory(int uid){
        UserHistory h = history.get(uid);
        if(Objects.isNull(h)){
            h=new UserHistory(uid);
            history.put(uid,h);
        }
        return h;
    }
}
