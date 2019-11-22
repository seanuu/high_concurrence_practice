package com.demo.nettyDemo.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

public class InHandlerTester {
    @Test
    public void testInHandlerLifeCircle() {
        final  InHandler inHandler = new InHandler();
        final  InHandler inHandler2 = new InHandler();

        ChannelInitializer initializer = new ChannelInitializer() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                channel.pipeline().addLast(inHandler);
            }
        };

        EmbeddedChannel channel = new EmbeddedChannel(initializer);

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);

        channel.writeInbound(buf);
        channel.flush();

        channel.writeInbound(buf);
        channel.flush();

        channel.close();
        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
