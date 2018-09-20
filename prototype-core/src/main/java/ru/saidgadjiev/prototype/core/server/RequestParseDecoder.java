package ru.saidgadjiev.prototype.core.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.saidgadjiev.prototype.http.lexer.HttpRequestLexer;
import ru.saidgadjiev.prototype.http.parser.HttpRequestParser;

import java.util.List;

/**
 * Created by said on 12.09.2018.
 */
public class RequestParseDecoder extends MessageToMessageDecoder<String> {

    protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) throws Exception {
        HttpRequestParser parser = new HttpRequestParser(new HttpRequestLexer(msg));

        out.add(parser.parse());
    }
}
