package com.demo.reactorModel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadReactor {
    private ServerSocketChannel serverSocketChannel;
    AtomicInteger atomicInteger = new AtomicInteger(0);
    private Selector[] selectors = new Selector[2];
    SubReactor[] subReactors;

    public static void main(String[] args) throws IOException {
        new MultiThreadReactor();
    }

    MultiThreadReactor() throws IOException {
        InetSocketAddress address = new InetSocketAddress(19000);
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(address);

        selectors[0] = Selector.open();
        selectors[1] = Selector.open();

        SelectionKey selectionKey = serverSocketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
        selectionKey.attach(new AcceptHandler());

        SubReactor subReactor1 = new SubReactor(selectors[0]);
        SubReactor subReactor2 = new SubReactor(selectors[1]);
        subReactors = new SubReactor[]{subReactor1, subReactor2};

        new Thread(subReactor1).start();
        new Thread(subReactor2).start();
    }

    class AcceptHandler implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel channel = serverSocketChannel.accept();

                if (channel != null) {
                    new MultiThreadHandler(selectors[atomicInteger.get()], channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (atomicInteger.incrementAndGet() == selectors.length) {
                atomicInteger.set(0);
            }
        }
    }

    class SubReactor implements Runnable {
        Selector selector;

        SubReactor(Selector s) {
            selector = s;
        }

        @Override
        public void run() {
            try {
                while (!Thread.interrupted()) {
                    selector.select();
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = keySet.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        dispatch(key);
                    }

                    keySet.clear();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void dispatch(SelectionKey key) {
            Runnable handler = (Runnable) key.attachment();

            if (handler != null) {
                handler.run();
            }
        }
    }
}
