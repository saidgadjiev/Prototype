package ru.saidgadjiev.prototype;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * Created by said on 12.09.2018.
 */
public class Decoder2 extends MessageToMessageDecoder<ByteBuf> {

    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        System.out.println("Yes");
    }
}
