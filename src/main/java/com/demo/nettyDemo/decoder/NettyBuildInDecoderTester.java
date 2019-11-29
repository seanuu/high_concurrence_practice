package com.demo.nettyDemo.decoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class NettyBuildInDecoderTester {
    @Test
    public void testLengthFiledBasedFrameDecoder() throws Exception {
        StringDecoder stringDecoder = new StringDecoder();

        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 4));
                channel.pipeline().addLast(stringDecoder);
                channel.pipeline().addLast(StringHandler.INSTANCE);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(initializer);

        String content = "hello world~";
        for (int i = 0; i < 50; i++) {
            ByteBuf buf = Unpooled.buffer();
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
            channel.writeInbound(buf);
        }

        channel.close();
    }
}
