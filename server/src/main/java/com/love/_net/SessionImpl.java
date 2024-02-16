package com.love._net;

import com.love._anno.NoRepo;
import com.love._api.*;
import com.love._api.Server;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SessionImpl implements Connection, Session {
    static final Logger logger = LoggerFactory.getLogger(SessionImpl.class);
    final Server server;
    final ChannelHandlerContext ctx;
    UserInfo user(){
        return server.userOperator().get(uid);
    }
    long lastActiveTime;
    SessionImpl(ChannelHandlerContext ctx, Server server){
        this.ctx = ctx;
        this.server = server;
        lastActiveTime = now();
    }

    @Override
    public Object fd() {
        return ctx;
    }
    @Override
    public void sendAndFlush(SendMsgBody msg) {
        lastActiveTime = now();
        sendAndFlush(server,this,msg);
    }
    static void sendAndFlush(Server server, SessionImpl session, SendMsgBody msg){
        WebSocketHandler.sendAndFlush(server,session,session.ctx,msg);
    }
    @Override
    public Session session() {
        return this;
    }
    RcvMsgReq lastRcvReq;
    @Override
    public void setLastRcvMsg(RcvMsgReq msg) {
        lastRcvReq = msg;
    }
    @Override
    public RcvMsgReq getLastRequest() {
        return lastRcvReq;
    }
    /***message sequence start**/
    @NoRepo
    int sequenceId;

    @Override
    public void msgIdIncrement() {
        final int reqSeq = lastRcvReq.header().sequenceId();
        if(!Server.debug){
            if(sequenceId+1!=reqSeq){
                logger.warn("sequence msg no matched");
                ctx.close();

            }
        }
        sequenceId = reqSeq+1;
    }
    /***message sequence end**/
    void kicked() {
        ctx.close();
    }
    int connLoginTimes;//for block ddos
    /***repeat login block control start**/
    boolean blockFlags = false;
    @Override
    public boolean isBlocked() {
        return blockFlags;
    }
    int blockFlagsCounter = 0;
    long blockTime = 0;
    @Override
    public void triggerBlockLogic(long now) {
        blockFlagsCounter++;
        if(blockFlagsCounter>3){
            blockFlags = true;
            blockFlagsCounter = 0;
            blockTime = now;
        }
    }
    static final long blockClearDuration = 60*1000;
    public void blockLogicTick(long now) {
        final long dur = blockClearDuration;
        if(now-blockTime>=dur){
            blockFlags = false;
        }
    }
    /***repeat login block control end**/
    @Override
    public Connection conn() {
        return this;
    }
    @Override
    public boolean loginDone() {
        return 0!= uid;
    }
    int uid;
    public int onUserLoginOk(int user) {
        int old = this.uid;
        this.uid = user;
        if(0!=old){
            logger.info("warning:user info exists");
        }
        return old;
    }
    public void onUserLogoutOk(int user) {
        if(user!=this.uid){
            logger.info("warning:logout but supplied user info no matched");
        }
        this.uid = 0;
    }

}
