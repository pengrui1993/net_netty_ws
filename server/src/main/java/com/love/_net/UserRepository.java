package com.love._net;

import com.love._api.Api;
import com.love._api.UserInfo;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UserRepository{
    final UserManager mgr;
    public UserRepository(UserManager userManager) {
        this.mgr = userManager;

    }
    int getUid(String uname){
        for (UserInfoItem info : infos) {
            if(Objects.equals(info.uname,uname)){
                return info.id;
            }
        }
        return 0;
    }
    void getUid(String uname,Consumer<Integer> c){
        final Consumer<UserInfo> cc =  user -> c.accept(user.id());
        final Executor e =  queue::offer;
        final long n = Api.now();
        requireUid(uname,id -> requireUserInfo(n,id,e,cc));
    }
    private void requireUid(String uname, Consumer<Integer> c) {
        c.accept(getUid(uname));
    }

    void checkPermission(CheckLogin msg,Consumer<Boolean> c){
        final Executor e =  queue::offer;
        final long n = Api.now();
        requireUid(msg.uname,id -> requireUserInfo(n, id, e, u -> c.accept(Objects.equals(u.pwd(),msg.pwd))));
    }
    boolean checkPermission(CheckLogin msg){
        for (UserInfoItem info : infos) {
            if(Objects.equals(info.uname,msg.uname)
                    &&Objects.equals(info.pwd,msg.pwd)){
                return true;
            }
        }
        return false;
    }
    UserInfoItem getDummyById(int id){
        for (UserInfoItem info : infos) {
            if(Objects.equals(info.id,id)){
                return info;
            }
        }
        return null;
    }

    static class UserInfoItem implements UserInfo{
        final int id;
        final String uname;
        final String pwd;
        public UserInfoItem(int id, String uname, String pwd) {
            this.id = id;
            this.uname = uname;
            this.pwd = pwd;
        }
        @Override
        public int id() {
            return id;
        }
        @Override
        public String pwd() {
            return pwd;
        }
    }
    final UserInfoItem[] infos = new UserInfoItem[]{
            new UserInfoItem(1,"abc","123456")
            ,new UserInfoItem(2,"def","123456")
            ,new UserInfoItem(3,"qwe","123456")
    };



    Map<Integer, UserInfo> users = new HashMap<>();
    Map<Integer,Long> lastAccessTime = new HashMap<>();
    void onTick(long now){
        final long dur = 30*60*1000;
        for (Integer uid : new ArrayList<>(lastAccessTime.keySet())) {
            long last = Optional.ofNullable(lastAccessTime.get(uid)).orElse(0L);
            if(now-last>dur){
                queue.offer(()->{
                    long l = Optional.ofNullable(lastAccessTime.get(uid)).orElse(0L);
                    if(now-l>dur){
                        lastAccessTime.remove(uid);
                        users.remove(uid);
                    }
                });
            }
        }
    }
    private UserInfo loadFromDatabase(int id){
        return getDummyById(id);
    }
    void requireUserInfo(
            long now,Executor e0, Consumer<UserInfo[]> c0
            ,Integer... ids
    ){
        final Executor e = Optional.ofNullable(e0).orElse(Runnable::run);
        final Consumer<UserInfo[]> c = Optional.ofNullable(c0).orElse(u -> {});
        if(ids.length<1){
            e.execute(()-> c.accept(new UserInfo[0]));
            return;
        }
        queue.offer(()->{
            final UserInfo[] us = new UserInfo[ids.length];
            for(int i=0;i<us.length;i++){
                Integer id = ids[i];
                UserInfo userInfo = users.get(id);
                if(Objects.isNull(userInfo)){
                    userInfo = loadFromDatabase(id);
                    users.put(id,userInfo);
                }
                lastAccessTime.put(id,now);
                us[i] = userInfo;
            }
            e.execute(()-> c.accept(us));
        });

    }
    void requireUserInfo(
            long now,Executor e, BiConsumer<UserInfo,UserInfo> c
            ,int id1,int id2
    ){
        requireUserInfo(now,id1, e,
                u1 -> requireUserInfo(now,id2, e, u2 -> c.accept(u1,u2)));
    }
    void requireUserInfo(long now,int id, Executor e, Consumer<UserInfo> c){
        requireUserInfo(now, e, us -> c.accept(us[0]),id);
    }
    static final Thread worker;
    static final BlockingDeque<Runnable> queue;
    static final Runnable loader;
    static final Thread main;
    static {
        main = Thread.currentThread();
        queue = new LinkedBlockingDeque<>();
        loader = () -> {
            while (true){
                try {
                    queue.take().run();
                } catch (InterruptedException ignore) {}
            }
        };
        worker = new Thread(loader);
        worker.setDaemon(true);
        worker.start();
    }

    void onUserNew(){
        onUserListChange(1);
    }
    void onUserDelete(){
        onUserListChange(-1);
    }
    void onUserUpdate(){
        onUserListChange(0);
    }
    int usersVersion;
    int usersSizeVersion;
    void onUserListChange(int type){
        usersVersion++;
        if(0!=type) usersSizeVersion++;
        //new delete,change the list
        //update change the item
    }

}
