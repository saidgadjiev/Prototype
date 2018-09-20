package ru.saidgadjiev.prototype.core.server;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

/**
 * Created by said on 15.09.2018.
 */
public class ResponseWriteHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ChannelFuture channelFuture = ctx.writeAndFlush(Unpooled.copiedBuffer((byte[])msg));

        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
}
