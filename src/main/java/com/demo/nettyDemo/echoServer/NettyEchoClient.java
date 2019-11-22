package com.demo.nettyDemo.echoServer;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.Scanner;

public class NettyEchoClient {
    private InetSocketAddress address;
    Bootstrap bootstrap = new Bootstrap();

    public static void main(String[] args) {
        new NettyEchoClient(19000).runClient();
    }

    NettyEchoClient(int port) {
        address = new InetSocketAddress(port);
    }

    public void runClient() {
        // 创建线程组
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // 设置线程组
            bootstrap.group(workerGroup);

            // 设置连接地址、通道类型
            bootstrap.remoteAddress(address);
            bootstrap.channel(NioSocketChannel.class);

            // 设置通道参数
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

            // 配置通道流水线
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(NettyEchoClientHandler.INSTANCE);
                }
            });

            // 连接服务器
            ChannelFuture future = bootstrap.connect();
            future.sync();

            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else {
                System.out.println("连接失败!");
            }

            Channel channel = future.channel();
            Scanner scanner = new Scanner(System.in);
            System.out.println("请输入发送内容:");

            while (scanner.hasNext()) {
                //获取输入的内容
                String next = scanner.next();
                byte[] bytes = (new Date().toString() + " >>" + next).getBytes("UTF-8");

                //发送ByteBuf
                ByteBuf buffer = channel.alloc().buffer();
                buffer.writeBytes(bytes);
                channel.writeAndFlush(buffer);
                System.out.println("请输入发送内容:");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
