package com.demo.socket;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class NIOSendClient {
    private Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws IOException, InterruptedException {
        NIOSendClient client = new NIOSendClient();
        client.sendFile();
    }

    public void sendFile() throws IOException, InterruptedException {
        String srcPath = "src/main/resources/dir/nio_file_copy.txt";
        String filename = "copy.txt";

        File file = new File(srcPath);
        if (!file.exists()) {
            file.createNewFile();
        }

        FileChannel fileChannel = new FileInputStream(file).getChannel();

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.socket().connect(new InetSocketAddress(19000));

        socketChannel.configureBlocking(false);

        while (!socketChannel.finishConnect()) {}

//        ByteBuffer filenameBuf = charset.encode(filename);
//        socketChannel.write(filenameBuf);
//        Thread.sleep(100);

        ByteBuffer buf = ByteBuffer.allocate(1024);
//        buf.putLong(file.length());
//        buf.flip();
//        socketChannel.write(buf);
//        buf.clear();

        int length = 0;
        long progress = 0;

        while ((length = fileChannel.read(buf)) > 0) {
            buf.flip();

            socketChannel.write(buf);
            buf.clear();

            progress += length;

            System.out.println("progress: " + (100 * progress / file.length()) + "%");
        }

        fileChannel.close();
        socketChannel.shutdownOutput();
        socketChannel.close();
    }

}
