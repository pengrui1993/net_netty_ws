package com.love._net;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ChannelManager extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(ChannelManager.class);
    static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        group.add(ctx.channel());
        super.channelActive(ctx);
        logger.debug("channelActive, all groups:{}",group);
    }
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        group.remove(ctx.channel());
        logger.debug("channelInactive, all groups:{}",group);
    }

    void test(ChannelHandlerContext ctx){
        ByteBufAllocator alloc = ctx.alloc();
        ByteBuf buf1 = alloc.buffer();
        ByteBuf buffer1 = Unpooled.buffer();
        ByteBuf buffer2 = UnpooledByteBufAllocator.DEFAULT.buffer(); //buffer1 == buffer2
        //
        ByteBuf byteBuf = Unpooled.directBuffer();
        ByteBuf byteBuf1 = alloc.directBuffer();
        //
        ByteBuf byteBuf2 = alloc.ioBuffer();//first use direct buf
    }
}