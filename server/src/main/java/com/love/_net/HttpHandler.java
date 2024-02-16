package com.love._net;

import com.love._api.Api;
import com.love._api.Server;
import com.love._api.NetOperator;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    final Logger logger = LoggerFactory.getLogger(this.getClass());
    final Server server;
    final NetOperator netOperator;
    public HttpHandler(Server server, NetOperator netOperator) {
        this.netOperator = netOperator;
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        final String uri = msg.uri();///api/http/gate?a=1
        if(!uri.startsWith(Api.HTTP_PREFIX)){
//            logger.info("ignore http api:"+msg);
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(msg.protocolVersion(), HttpResponseStatus.FORBIDDEN);
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
//            URI u = new URI(msg.uri());
//            logger.info(u.getPath());
//            HttpPostRequestDecoder pd = new HttpPostRequestDecoder(msg);//for post request parsing
            return;
        }
        httpLogic(ctx,msg);
    }
    private void httpLogic(ChannelHandlerContext ctx, FullHttpRequest req){
//        logger.info("NetManager.httpLogic:"+req.uri());
//        logger.info(req.toString());
//        QueryStringDecoder d = new QueryStringDecoder(req.uri());

        ByteBufAllocator alloc = ctx.alloc();
        boolean keepAlive = HttpUtil.isKeepAlive(req);
        FullHttpResponse response = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.OK,
                Unpooled.wrappedBuffer("hello world".getBytes(StandardCharsets.UTF_8)));
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
                .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        if (keepAlive) {
            if (!req.protocolVersion().isKeepAliveDefault()) {
                response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            }
        } else {// Tell the client we're going to close the connection.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }
        ChannelFuture f = ctx.write(response);
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
        ctx.flush();
    }
}
