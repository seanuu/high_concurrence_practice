package com.demo.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.Scanner;

public class NIODatagramClient {
    public static void main(String[] args) throws IOException {
        new NIODatagramClient().send();
    }

    private void send() throws IOException {
        DatagramChannel dChannel = DatagramChannel.open();
        dChannel.configureBlocking(false);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        Scanner scanner = new Scanner(System.in);

        while(scanner.hasNext()){
            String string = scanner.next();
            buffer.put((new Date().toString()+">>"+string).getBytes());
            buffer.flip();

            dChannel.send(buffer, new InetSocketAddress("127.0.0.1", 8989));
            buffer.clear();
        }
        dChannel.close();
    }
}
