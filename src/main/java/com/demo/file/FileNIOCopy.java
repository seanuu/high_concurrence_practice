package com.demo.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class FileNIOCopy {

    public static void main(String[] args) throws IOException {
        String sourcePath = "src/main/resources/nio_file.txt";
        String targetPath = "src/main/resources/nio_file_copy.txt";
//        String sourcePath = "src/main/resources/exercise-easy-open.png";
//        String targetPath = "src/main/resources/exercise-easy-open-copy.png";

        nioCopyFile(sourcePath, targetPath);
//        nioFastCopyFile(sourcePath, targetPath);
    }

    private static void nioCopyFile(String srcPath, String destPath) throws IOException {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);

        Charset charset = StandardCharsets.UTF_8;
        CharsetDecoder decoder = charset.newDecoder();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        long startTime = System.currentTimeMillis();

        FileInputStream fis = null;
        FileOutputStream fos = null;

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        fis = new FileInputStream(srcFile);
        fos = new FileOutputStream(destFile);

        inChannel = fis.getChannel();
        outChannel = fos.getChannel();

        int length = -1;
        ByteBuffer buf = ByteBuffer.allocate(1024);

        while ((length = inChannel.read(buf)) != -1) {
            buf.flip();

            int outLength = 0;

            while ((outLength = outChannel.write(buf)) != 0) {
                System.out.println("out: " + outLength);
            }

            buf.clear();
        }

        outChannel.force(true);

        fis.close();
        fos.close();
        inChannel.close();
        outChannel.close();

        long endTime = System.currentTimeMillis();
        System.out.println("time:" + (endTime - startTime) + "ms");
    }

    private static void nioFastCopyFile(String srcPath, String destPath) throws IOException {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        long startTime = System.currentTimeMillis();

        FileInputStream fis = null;
        FileOutputStream fos = null;

        FileChannel inChannel = null;
        FileChannel outChannel = null;

        fis = new FileInputStream(srcFile);
        fos = new FileOutputStream(destFile);

        inChannel = fis.getChannel();
        outChannel = fos.getChannel();

        long size = inChannel.size();
        long count = 0;
        long pos = 0;

        while (pos < size) {
            count = size - pos > 2048 ? 2048 : size - pos;

            pos += outChannel.transferFrom(inChannel, pos, count);
        }
        outChannel.force(true);

        fis.close();
        fos.close();

        inChannel.close();
        outChannel.close();


        long endTime = System.currentTimeMillis();
        System.out.println("time:" + (endTime - startTime) + "ms");
    }
}
