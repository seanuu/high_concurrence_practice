package com.demo.reactorModel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiThreadHandler implements Runnable {
    SocketChannel socketChannel;
    SelectionKey selectionKey;
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    private static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;

    static ExecutorService pool = Executors.newFixedThreadPool(4);

    MultiThreadHandler(Selector selector, SocketChannel channel) throws IOException {
        this.socketChannel = channel;
        channel.configureBlocking(false);

        selectionKey = channel.register(selector, 0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    @Override
    public void run() {
        pool.execute(new AsyncTask());
    }

    public synchronized void asyncRun() throws IOException {
        if (MultiThreadHandler.this.selectionKey.isReadable()) {
            int length = 0;
            while ((length = socketChannel.read(buffer)) > 0) {
                buffer.flip();
                System.out.println("RECEIVING: " + new String(buffer.array(), 0, length));
                buffer.clear();
            }

            selectionKey.interestOps(SelectionKey.OP_WRITE);
        } else if (MultiThreadHandler.this.selectionKey.isWritable()) {
            String text = new Date().toString();
            System.out.println("SENDING: " + text);

            buffer.put(text.getBytes());
            buffer.flip();

            socketChannel.write(buffer);
            buffer.clear();

            selectionKey.interestOps(SelectionKey.OP_READ);
        }
    }

    class AsyncTask implements Runnable {
        @Override
        public void run() {
            try {
                MultiThreadHandler.this.asyncRun();
            } catch (IOException e) {
                MultiThreadHandler.this.selectionKey.cancel();
                e.printStackTrace();
            }
        }
    }
}
