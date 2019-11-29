package com.demo.nettyDemo.encoder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class NettyBuildInEncoderTester {
    @Test
    public void testPrependerEncoder() throws Exception {

        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new LengthFieldPrepender(4));
                channel.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(initializer);

        String content = "hello world~";

        for (int i = 0; i < 5; i++) {
            channel.write(content);
        }
        channel.flush();

        ByteBuf buf = (ByteBuf) channel.readOutbound();
        while (buf != null) {
            System.out.println("o = " + buf.readableBytes());
            buf = (ByteBuf) channel.readOutbound();
        }
    }
}
