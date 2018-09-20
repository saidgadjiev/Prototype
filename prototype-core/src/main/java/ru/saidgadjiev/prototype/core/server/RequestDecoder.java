package ru.saidgadjiev.prototype.core.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import ru.saidgadjiev.prototype.core.component.RestClass;
import ru.saidgadjiev.prototype.core.component.RestMethod;
import ru.saidgadjiev.prototype.http.HttpRequest;
import ru.saidgadjiev.prototype.http.HttpResponse;
import ru.saidgadjiev.prototype.http.HttpStatus;

import java.util.*;

/**
 * Created by said on 12.09.2018.
 */
public class RequestDecoder extends MessageToMessageDecoder<HttpRequest> {

    private final Collection<RestClass> restHandlerMap;

    public RequestDecoder(Collection<RestClass> restHandlerMap) {
        this.restHandlerMap = restHandlerMap;
    }

    protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) throws Exception {
        Optional<RestMethod> restMethodHandlerOptional = getAppropriateMethod(msg.getUri());

        if (restMethodHandlerOptional.isPresent()) {
            RestMethod methodHandler = restMethodHandlerOptional.get();

            if (methodHandler.probe(msg.getMethod())) {
                Collection<RestMethod.RestParam> list = methodHandler.getParams();
                Collection<Object> methodParams = new ArrayList<>();

                for (RestMethod.RestParam param: list) {
                    methodParams.add(param.parse(msg));
                }

                RestMethod.RestResult result = methodHandler.invoke(methodParams);

                System.out.println("result = " + result);

                ctx.channel().write(new HttpResponse(HttpStatus.OK, result));
            }
        }
    }

    private Optional<RestMethod> getAppropriateMethod(String uri) {
        for (RestClass handlerEntry : restHandlerMap) {
            if (uri.startsWith(handlerEntry.getUrl())) {
                for (RestMethod methodHandlerEntry : handlerEntry.getRestMethods()) {
                    if ((handlerEntry.getUrl() + methodHandlerEntry.getUri()).equals(uri)) {
                        return Optional.of(methodHandlerEntry);
                    }
                }
            }
        }

        return Optional.empty();
    }
}
