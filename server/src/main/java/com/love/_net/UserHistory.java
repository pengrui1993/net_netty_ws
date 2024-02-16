package com.love._net;

class UserHistory {
    final int uid;
    long lastLoginFailureTime = -1;
    long lastLoginSuccessTime;
    int totalErrorTimes;
    int errPwdWhenLoginCounter;
    boolean locked;
    UserHistory(int uid) {
        this.uid = uid;
    }
    void loginSuccess(long now) {
        lastLoginSuccessTime = now;
        totalErrorTimes+= errPwdWhenLoginCounter;
        errPwdWhenLoginCounter = 0;
    }
    void loginFailure(int limit, long now){
        lastLoginFailureTime = now;
        if(++errPwdWhenLoginCounter >=limit){
            locked = true;
        }
    }
    boolean isLocked() {
        return locked;
    }
    void resetLock() {
        totalErrorTimes+= errPwdWhenLoginCounter;
        errPwdWhenLoginCounter = 0;
        locked = false;
    }

    @Override
    public String toString() {
        return "UserHistory{" +
                "uid=" + uid +
                ", lastLoginFailureTime=" + lastLoginFailureTime +
                ", lastLoginSuccessTime=" + lastLoginSuccessTime +
                ", totalErrorTimes=" + totalErrorTimes +
                ", errPwdWhenLoginCounter=" + errPwdWhenLoginCounter +
                ", locked=" + locked +
                '}';
    }
}
