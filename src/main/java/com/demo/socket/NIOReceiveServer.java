package com.demo.socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NIOReceiveServer {
    private Charset charset = Charset.forName("UTF-8");

    static class Client {
        String filename;

        long fileLength;

        long startTime;

        InetSocketAddress remoteAddress;

        FileChannel outChannel;
    }

    Map<SelectableChannel, Client> clientMap = new HashMap<SelectableChannel, Client>();

    public static void main(String[] args) throws IOException {
        NIOReceiveServer server = new NIOReceiveServer();
        server.startServer();
    }

    public void startServer() throws IOException {
        // 1、获取Selector选择器
        Selector selector = Selector.open();

        // 2、获取通道
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        ServerSocket serverSocket = serverChannel.socket();

        // 3.设置为非阻塞
        serverChannel.configureBlocking(false);

        // 4、绑定连接
        InetSocketAddress address = new InetSocketAddress(19000);
        serverSocket.bind(address);

        // 5、将通道注册到选择器上,并注册的IO事件为：“接收新连接”
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("serverChannel is listening...");

        // 6、轮询感兴趣的I/O就绪事件（选择键集合）
        while (selector.select() > 0) {
            // 7、获取选择键集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                // 8、获取单个的选择键，并处理
                SelectionKey key = iterator.next();

                // 9、判断key是具体的什么事件，是否为新连接事件
                if (key.isAcceptable()) {
                    // 10、“新连接”事件,获取客户端新连接
                    ServerSocketChannel server = (ServerSocketChannel) key.channel();
                    SocketChannel socketChannel = server.accept();

                    if (socketChannel == null) continue;

                    // 11、客户端新连接，切换为非阻塞模式
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    // 余下为业务处理
                    Client client = new Client();

                    client.remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                    clientMap.put(socketChannel, client);
                } else if (key.isReadable()) {
                    processData(key);
                    System.out.println("socketChannel");
                }
                // NIO的特点只会累加，已选择的键的集合不会删除
                // 如果不删除，下一次又会被select函数选中
                iterator.remove();
            }
        }
    }

    private void processData(SelectionKey key) throws IOException {
        Client client = clientMap.get(key.channel());
        ByteBuffer buf = ByteBuffer.allocate(1024 * 10);

        SocketChannel socketChannel = (SocketChannel) key.channel();
        int length = 0;
        buf.clear();

        while ((length = socketChannel.read(buf)) > 0) {
            buf.flip();

            if (client.filename == null) {
                client.startTime = System.currentTimeMillis();
                String destPath = "src/main/resources/copy.txt";
                client.filename = destPath;

                File file = new File(destPath);
                if (!file.exists()) {
                    file.createNewFile();
                }

                client.outChannel = new FileOutputStream(file).getChannel();

                client.outChannel.write(buf);
            } else {
                client.outChannel.write(buf);
            }

            buf.clear();
        }

        if (length == -1) {
            client.outChannel.close();
            System.out.println("上传完毕");
            key.cancel();

            long endTime = System.currentTimeMillis();
            System.out.println("NIO IO 传输毫秒数：" + (endTime - client.startTime));
        }

    }


}
