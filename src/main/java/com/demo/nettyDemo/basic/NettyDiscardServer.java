package com.demo.nettyDemo.basic;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyDiscardServer {
    private final int serverPort;
    ServerBootstrap bootstrap = new ServerBootstrap();

    public static void main(String[] args) throws Exception {
        new NettyDiscardServer(19000).runServer();
    }

    NettyDiscardServer(int port) {
        serverPort = port;
    }

    public void runServer() throws Exception {
        EventLoopGroup bossLoopGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerLoopGroup = new NioEventLoopGroup();

        try {
            // 设置反应器线程组 parentGroup  childGroup
            bootstrap.group(bossLoopGroup, workerLoopGroup);
            // 设置nio类型的通道
            bootstrap.channel(NioServerSocketChannel.class);
            // 设置监听端口
            bootstrap.localAddress(serverPort);
            // 设置通道的参数
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            // 装配子通道流水线
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                // 有连接到达时
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    // 流水线管理子通道中的Handler处理器
                    socketChannel.pipeline().addLast(new NettyDiscardHandler());
                }
            });

            // 绑定服务器
            // 通过调用sync同步方法阻塞直到绑定成功
            ChannelFuture channelFuture = bootstrap.bind().sync();
            System.out.println("服务器启动成功，监听端口：" + channelFuture.channel().localAddress());

            // 等待通道关闭的异步任务结束
            // 服务监听通道会一直等待通道关闭的异步任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();
        } finally {
            // 关闭 EventLoopGroup
            // 释放掉所有资源包括创建的线程
            workerLoopGroup.shutdownGracefully();
            bossLoopGroup.shutdownGracefully();
        }
    }
}
