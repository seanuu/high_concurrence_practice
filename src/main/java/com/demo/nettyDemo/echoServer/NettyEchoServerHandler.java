package com.demo.nettyDemo.echoServer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;

@ChannelHandler.Sharable
public class NettyEchoServerHandler extends ChannelInboundHandlerAdapter {
    final static NettyEchoServerHandler INSTANCE = new NettyEchoServerHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        System.out.println("msg type: " + (in.hasArray() ? "堆内存" : "直接内存"));

        byte[] array = new byte[in.readableBytes()];
        in.getBytes(0, array);
        System.out.println("server received: " + new String(array, StandardCharsets.UTF_8));

        // 写回数据
        System.out.println("写回前，msg.refCnt:" + ((ByteBuf) msg).refCnt());
        ChannelFuture channelFuture = ctx.writeAndFlush(msg);
        channelFuture.addListener((ChannelFuture future) -> {
            System.out.println("写回后，msg.refCnt:" + ((ByteBuf) msg).refCnt());
        });
    }
}
