package com.demo.nettyDemo.echoServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyEchoServer {
    private final int port;
    ServerBootstrap bootstrap = new ServerBootstrap();

    public static void main(String[] args) {
        new NettyEchoServer(19000).runServer();
    }

    NettyEchoServer(int port) {
        this.port = port;
    }

    public void runServer() {
        // 创建反应器线程组
        EventLoopGroup parentGroup = new NioEventLoopGroup(1);
        EventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            // 设置反应器线程组
            bootstrap.group(parentGroup, childGroup);

            // 设置监听端口、通道类型
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.localAddress(port);

            // 设置父子通道参数
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            // 装配子通道流水线
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(NettyEchoServerHandler.INSTANCE);
                }
            });

            // 阻塞 绑定服务器任务结束
            ChannelFuture channelFuture = bootstrap.bind().sync();
            System.out.println("服务器启动成功，监听端口：" + channelFuture.channel().localAddress());

            // 阻塞 等待服务器关闭任务结束
            ChannelFuture closeFuture = channelFuture.channel().closeFuture();
            closeFuture.sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭EventLoopGroup、释放线程
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        }
    }
}
