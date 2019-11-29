package com.demo.nettyDemo.decoderEncoder;

import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyCombined extends CombinedChannelDuplexHandler<StringDecoder, StringEncoder> {

    public NettyCombined() {
        // 组合字符串 解码、编码器
        super(new StringDecoder(), new StringEncoder());
    }
}
