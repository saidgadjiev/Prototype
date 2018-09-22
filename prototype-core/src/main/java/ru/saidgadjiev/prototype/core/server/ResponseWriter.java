package ru.saidgadjiev.prototype.core.server;

import io.netty.channel.*;

/**
 * Created by said on 21.09.2018.
 */
public class ResponseWriter extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ChannelFuture channelFuture = ctx.writeAndFlush(msg);

        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }
}
