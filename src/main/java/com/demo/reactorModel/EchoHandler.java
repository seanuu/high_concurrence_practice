package com.demo.reactorModel;

import com.sun.org.apache.regexp.internal.RE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;

public class EchoHandler implements Runnable {
    private final SocketChannel socketChannel;
    private final SelectionKey selectionKey;
    private static final int RECEIVING = 0, SENDING = 1;
    int state = RECEIVING;

    EchoHandler(Selector selector, SocketChannel sk) throws IOException {
        socketChannel = sk;
        sk.configureBlocking(false);

        selectionKey = socketChannel.register(selector, 0);
        selectionKey.attach(this);

        selectionKey.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
    }

    public void run() {
        try {
            if (state == RECEIVING) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                int length = 0;
                while ((length = socketChannel.read(buffer)) > 0) {
                    buffer.flip();
                    System.out.println("RECEIVING: " + new String(buffer.array(), 0, length));
                    buffer.clear();
                }

                selectionKey.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            } else if (state == SENDING) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                String text = new Date().toString();
                System.out.println("SENDING: " + text);

                buffer.put(text.getBytes());
                buffer.flip();

                socketChannel.write(buffer);
                buffer.clear();

                selectionKey.interestOps(SelectionKey.OP_READ);
                state = RECEIVING;
            }
        } catch (IOException e) {
            selectionKey.cancel();
            e.printStackTrace();
        }
    }
}
