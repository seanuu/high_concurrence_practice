package com.demo.reactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        new EchoClient().start();
    }

    public void start() throws IOException {
        InetSocketAddress address = new InetSocketAddress(19000);

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(address);

        while (!socketChannel.finishConnect()) {
        }

        System.out.println("客户端启动成功");

        Processor processor = new Processor(socketChannel);
        new Thread(processor).start();
    }

    class Processor implements Runnable {
        Selector selector;
        SocketChannel socketChannel;

        Processor(SocketChannel channel) throws IOException {
            selector = Selector.open();
            socketChannel = channel;

            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selected.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();

                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            SocketChannel channel = (SocketChannel) key.channel();
                            int length = 0;

                            while ((length = channel.read(buffer)) > 0) {
                                buffer.flip();

                                System.out.println("SERVER ECHO:" + new String(buffer.array(), 0, length));

                                buffer.clear();
                            }
                        } else if (key.isWritable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(1024);
                            Scanner scanner = new Scanner(System.in);
                            SocketChannel channel = (SocketChannel) key.channel();

                            System.out.println("请输入内容：");
                            if (scanner.hasNext()) {
                                String text = scanner.next();

                                buffer.put((new Date().toString() + ">>" + text).getBytes());
                                buffer.flip();

                                channel.write(buffer);
                                buffer.clear();
                            }
                        }
                    }

                    selected.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
