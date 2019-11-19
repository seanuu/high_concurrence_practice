package com.demo.reactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoSeverReactor implements Runnable {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public static void main(String[] args) throws IOException {
        new Thread(new EchoSeverReactor()).start();
    }

    private EchoSeverReactor() throws IOException {
        InetSocketAddress address = new InetSocketAddress(19000);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        ServerSocket serverSocket = serverSocketChannel.socket();
        serverSocket.bind(address);

        selector = Selector.open();

        SelectionKey acceptKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        acceptKey.attach(new AcceptHandler());
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                if (selector.select() > 0) {
                    Set<SelectionKey> selected = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selected.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        dispatch(key);
                    }

                    selected.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable handler = (Runnable) key.attachment();

        if (handler != null) {
            handler.run();
        }
    }

    class AcceptHandler implements Runnable {
        public void run () {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();

                if (socketChannel != null) {
                    System.out.println("connect start");
                    new EchoHandler(selector, socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
