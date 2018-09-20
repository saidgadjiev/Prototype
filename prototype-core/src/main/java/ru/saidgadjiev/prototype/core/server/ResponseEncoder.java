package ru.saidgadjiev.prototype.core.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.saidgadjiev.prototype.http.HttpResponse;

import java.util.List;

/**
 * Created by said on 15.09.2018.
 */
public class ResponseEncoder extends MessageToMessageEncoder<HttpResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, HttpResponse msg, List<Object> out) throws Exception {
        out.add(toResponse(msg).getBytes());
    }

    private String toResponse(HttpResponse response) {
        StringBuilder builder = new StringBuilder();

        builder.append(response.getHttpVersion());
        builder.append(" ");
        builder.append(response.getStatus().getCode());
        builder.append(response.getStatus().getReason());
        builder.append("\r\n");
        builder.append("Content-Length: ").append(response.getBody().getLength());
        builder.append("\r\n");
        builder.append("\r\n");
        builder.append(response.getBody().toBody());

        return builder.toString();
    }

}
