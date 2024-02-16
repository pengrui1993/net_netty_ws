package com.love._net;

import com.love._api.*;
import com.love._core.Event;
import com.love._core.Listener;
import com.love._api.Server;
import com.love._evt.*;
import com.love._util.LogTrace;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;


public class NetManager implements NetOperator {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Server server;
    final Listener start = evt->init();
    final Listener stop = evt->destroy();
    final Listener tick = evt->onTick(SysTickEvent.class.cast(evt));
    final SessionManager sessionManager;
    boolean working;
    public NetManager(Server server) {
        this.server = server;
        sessionManager = new SessionManager(server,this);
        working = false;
        server.dispatcher().on(Event.Type.SYS_START,start);
        server.dispatcher().on(Event.Type.SYS_STOP,stop);
    }

    private Channel serverChannel;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    void init(){
        if(working)return;
        if(Objects.nonNull(bossGroup)){
            logger.info("NetManager.init ignore request");
            return;
        }
        class NettyLogging extends LoggingHandler{
            NettyLogging(){
                super(LogLevel.INFO);
            }
            @Override
            public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                LogTrace.enable("boos");
                super.handlerAdded(ctx);
            }
            @Override
            public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                super.handlerRemoved(ctx);
                LogTrace.disable();
            }
        }
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new NettyLogging())
                .childHandler(new Initializer());
//        ;
        //标识当服务器请求处理线程全满时，用于临时存放已完成三次握手的请求的队列的最大长度
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        //Netty4使用对象池，重用缓冲区
        bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        //是否启用心跳保活机制
//        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        //禁止使用Nagle算法，便于小数据即时传输
//        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        bootstrap.bind(1234).addListener((ChannelFutureListener) future -> {
            serverChannel = future.channel();
            server.post(()->server.dispatcher().on(Event.Type.SYS_TICK,tick));//TODO concurrent
            logger.info("async binding ok");
        });
        working = true;
        logger.info("async binding");
    }
    long lastPrintTime;
    void onTick(SysTickEvent e){
        if(e.getLast()-lastPrintTime>20000){
            lastPrintTime = e.getLast();
        }
    }
    void destroy(){
        if(!working)return;
        ChannelFuture future = serverChannel.close();
        try {
            future.sync();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(),e);
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        serverChannel = null;
        bossGroup = null;
        workerGroup = null;
        server.dispatcher().off(Event.Type.SYS_TICK,tick);
        server.dispatcher().off(Event.Type.SYS_STOP,stop);
        server.dispatcher().off(Event.Type.SYS_START,start);
        logger.info("need to be destroy");
    }
    @Override
    public void onClose(Object ctx, int flag) {
        sessionManager.netClose(ctx,flag);
    }
    @Override
    public Connection createConn(Object ctx) {
        return sessionManager.netCreate(ctx);
    }
    @Override
    public Connection getConn(Object ctx) {
        return sessionManager.getConn(ctx);
    }

    @Override
    public boolean post(Runnable runnable) {
        return NetThread.enqueue(runnable);
    }
    Set<Object> wsChannel = new HashSet<>();
    @Override
    public boolean isHttpChannel(Object ctx) {
        return !isWsChannel(ctx);
    }
    @Override
    public boolean isWsChannel(Object ctx) {
        return wsChannel.contains(ctx);
    }
    @Override
    public void addWsChannel(Object ctx) {
        wsChannel.add(ctx);
    }
    @Override
    public void removeWsChannel(Object ctx) {
        wsChannel.remove(ctx);
    }
    private static final String wsPath = Api.WS_GATE_PATH;



    class Initializer extends ChannelInitializer<SocketChannel>{
        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("Initializer.handlerAdded , thread id:"+Thread.currentThread().getId());
            super.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            super.handlerRemoved(ctx);
            LogTrace.disable();
        }
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            LogTrace.enable("init");
            final ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(new ChannelManager());
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(65536));
            //https://www.cnblogs.com/vettel0329/p/12221268.html
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new WebSocketServerCompressionHandler());
            pipeline.addLast(new WebSocketServerProtocolHandler(wsPath,null,true));
            pipeline.addLast(new HttpHandler(server,NetManager.this)); //channel active socket first , http second
            pipeline.addLast(new WebSocketHandler(server,NetManager.this));

        }
    }

    static class Test implements Server{
        final ConcurrentLinkedQueue<Runnable>queue = new ConcurrentLinkedQueue<>();
        final TopEventDispatcher ed = new TopEventDispatcher();
        final UserManager um = new UserManager(this);
        final NetManager net = new NetManager(this);
        @Override
        public EventDispatcher dispatcher() {
            return ed;
        }
        @Override
        public UserOperator userOperator() {
            return um;
        }
        @Override
        public NetOperator netOperator() {
            return net;
        }
        @Override
        public boolean post(Runnable runnable) {
            return queue.offer(runnable);
        }
        static void sleep(long l){
            try {
                Thread.sleep(l);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        public static void main(String[] args) {
            final Test server = new Test();
            final Logger logger = LoggerFactory.getLogger(server.getClass());
            EventDispatcher ed = server.dispatcher();
            final SysTickEvent tick = new SysTickEvent();
            ed.emit(new SysStartEvent());
            int count = 0;
            while(count++<10){
                ed.emit(tick.reuse());
                Runnable runner;
                while(Objects.nonNull(runner = server.queue.poll()))runner.run();
                sleep(1000);
            }
            ed.emit(new SysStopEvent());
            logger.info("NetManager.main done");
        }
    }
}
