package com.demo.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class NIODatagramServer {
    public static void main(String[] args) throws IOException {
        new NIODatagramServer().receive();
    }

    private void receive() throws IOException {
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(new InetSocketAddress(8989));

        Selector selector = Selector.open();
        datagramChannel.register(selector, SelectionKey.OP_READ);

        while(selector.select()>0){
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = (SelectionKey) iterator.next();

                if (selectionKey.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    SocketAddress address = datagramChannel.receive(buffer);
                    buffer.flip();

                    System.out.println("address:" + address + "| message:" + new String(buffer.array(),0, buffer.limit()));
                    buffer.clear();
                }
            }
            iterator.remove();
        }
    }
}