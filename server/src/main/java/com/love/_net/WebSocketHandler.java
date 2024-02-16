package com.love._net;

import com.love._api.*;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.ConnectedEvent;
import com.love._api.EventDispatcher;
import com.love._evt.NetMsgRcvEvent;
import com.love._evt.UserLoginOkEvent;
import com.love._evt.UserLogoutOkEvent;
import com.love._net.send.ConnPreparingResultBody;
import com.love._net.send.RspMsgHeader;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.love._net.NetManager.*;
public class WebSocketHandler extends SimpleChannelInboundHandler<WebSocketFrame>{
    final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    final NetOperator netOperator;
    final Server server;
    Object uid;
    final Listener onUserLoginOk = (evt)->{
        final UserLoginOkEvent e = UserLoginOkEvent.class.cast(evt);
        uid = e.uid;
    };
    final Listener onUserLogoutOk = (evt)-> {
        UserLogoutOkEvent e = UserLogoutOkEvent.class.cast(evt);
        if(!Objects.equals(e.uid,uid))
            logger.info("no matched logout,old:{},new:{}",uid,e.uid);
        logger.info("log out ok uid:"+e.uid);
        uid = null;
    };
    public WebSocketHandler(Server server, NetOperator netOperator) {
        this.netOperator = netOperator;
        this.server = server;
    }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(netOperator.isWsChannel(ctx)){
            netOperator.removeWsChannel(ctx);
            server.post(()->{
                netOperator.onClose(ctx,INACTIVE_FLAG);
                server.post(()->{
//                    logger.info("unregister handler login logout listener");
                    final EventDispatcher dispatcher = server.dispatcher();
                    print("ws disconnected pre");
                    dispatcher.off(Event.Type.USER_LOGIN_OK,onUserLoginOk);
                    dispatcher.off(Event.Type.USER_LOGOUT_OK,onUserLogoutOk);
                    print("ws disconnected post");
                });
            });
        }
    }
    Map<Event.Type, List<Listener>> c  = new HashMap<>();
    boolean ignorePrint = true;
    void print(String prefix){
        if(!Server.debug)return;
        if(ignorePrint)return;
        c.clear();
        server.dispatcher().channels(c);
        final List<Listener> list = c.values().stream().flatMap(Collection::stream).toList();
        StringBuilder builder = new StringBuilder(prefix+","+list.size());
        c.clear();
        server.dispatcher().onTemp(c);
        builder.append(",on temp:").append(c.size());
        c.clear();
        server.dispatcher().offTemp(c);
        builder.append(",off temp:").append(c.size());
        logger.info(builder.toString());
        c.clear();
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete wsHc) {
            netOperator.addWsChannel(ctx);
            server.post(()->{
//                logger.info("register handler on ws to listen the login logout");
                final EventDispatcher dispatcher = server.dispatcher();
                dispatcher.emit(new ConnectedEvent(netOperator.createConn(ctx)));
                print("ws handshake pre");
                dispatcher.on(Event.Type.USER_LOGIN_OK,onUserLoginOk);
                dispatcher.on(Event.Type.USER_LOGOUT_OK,onUserLogoutOk);
                print("ws handshake post");

            });
            logger.info("handshake ok ,thread id:{}",Thread.currentThread().getId());
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(),cause);
        ctx.close();
    }
    static class DummyHeader extends SendMsgHeader.Generic{
        static DummyHeader INSTANCE = new DummyHeader();
        DummyHeader(){cmd = -1;}
    }
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled
        if (frame instanceof TextWebSocketFrame) {
            final Connection conn = netOperator.getConn(ctx);
            if(Objects.isNull(conn)){//conn preparing
                final SendMsgBody body =  ConnPreparingResultBody.INSTANCE;
                final DummyHeader header = DummyHeader.INSTANCE;
                final Object rsp = JsonProtocol.wrapper(body,header);
                sendAndFlush(server,ctx,rsp);
                logger.warn("ignore preparing conn,channel ctx:"+ctx);
                return;
            }
            if(conn.session().isBlocked()){
                logger.warn("session blocked");
                return;
            }
            // Send the uppercase string back.
            final String request = ((TextWebSocketFrame) frame).text();
            final Runnable messaging = ()->{
                EventDispatcher ed = server.dispatcher();
                server.post(() -> ed.emit(new NetMsgRcvEvent(JsonProtocol.en(request), conn)));
            };
            if(netOperator.usePost){
                final boolean b = netOperator.post(messaging);
                if(!b)logger.info("request enqueue failure msg:"+request);
            }else{
                messaging.run();
            }
//            ctx.channel().writeAndFlush(new TextWebSocketFrame(request.toUpperCase(Locale.US)));
        }else if(frame instanceof BinaryWebSocketFrame bf){
            logger.info("binary frame:"+bf.content());
        }else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }
    public static void sendAndFlush(Server server,Session session
            ,ChannelHandlerContext ctx,SendMsgBody msg) {
        sendAndFlush(server,ctx,JsonProtocol.wrapper(msg,session));
    }
    static void sendAndFlush(Server server,ChannelHandlerContext ctx,Object msg){
        final Runnable runner = ()->{
            ctx.writeAndFlush(msg);
//            ctx.channel().write(wrapper(msg,this));
//            ctx.channel().writeAndFlush(wrapper(msg,this));
        };
        final NetOperator no = server.netOperator();
        if(no.usePost){
            no.post(runner);
        }else{
            runner.run();
        }
    }
    public static void main(String[] args) throws IOException {
        final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
        final BlockingDeque<Runnable> queue = new LinkedBlockingDeque<>();
        final AtomicBoolean running = new AtomicBoolean(true);
        final Thread t = new Thread(()->{
            logger.info(Thread.currentThread().toString());
            while(running.get()){
                try {
                    logger.info("blocking...");
                    queue.take().run();
                    logger.info("pass run...");
                } catch (InterruptedException ignore) {
                }
            }
        });
        logger.info(t+",in main");
        t.setDaemon(true);
        t.start();
        System.in.read();
        logger.info("quit");
    }
}
