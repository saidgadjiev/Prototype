package ru.saidgadjiev.prototype.core.server;

import com.google.gson.GsonBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import ru.saidgadjiev.prototype.core.codec.UriAttributesDecoder;
import ru.saidgadjiev.prototype.core.component.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Created by said on 12.09.2018.
 */
public class RequestHandler extends MessageToMessageDecoder<FullHttpRequest> {

    private final BeanProcessor beanProcessor;

    private GsonBuilder gsonBuilder;

    public RequestHandler(BeanProcessor beanProcessor, GsonBuilder gsonBuilder) {
        this.beanProcessor = beanProcessor;
        this.gsonBuilder = gsonBuilder;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        RestMethodResult restMethodResult = getAppropriateMethod(request.method(), queryStringDecoder.path());

        if (restMethodResult.getStatus().equals(HttpResponseStatus.OK)) {
            RestMethod methodHandler = restMethodResult.getMethod();
            UriAttributesDecoder uriAttributesDecoder = restMethodResult.getUriAttributesDecoder();

            Collection<RestMethod.RestParam> list = methodHandler.getParams();
            Collection<Object> methodParams = new ArrayList<>();
            HttpRequestContext context;

            if (request.method() == HttpMethod.GET) {
                context = createContext(queryStringDecoder, request, uriAttributesDecoder, null);
            } else {
                context = createContext(queryStringDecoder, request, uriAttributesDecoder, new HttpPostRequestDecoder(request));
            }

            try {
                for (RestMethod.RestParam param : list) {
                    RestMethod.RequiredResult requiredResult = param.checkRequired(context);

                    if (requiredResult.getStatus().equals(HttpResponseStatus.OK)) {
                        methodParams.add(param.convert(context));
                    } else {
                        sendResponse(ctx, request, requiredResult.getStatus(), null, null);

                        break;
                    }
                }
            } finally {
                if (context.getHttpPostRequestDecoder() != null) {
                    context.getHttpPostRequestDecoder().cleanFiles();
                }
            }

            RestMethod.RestResult result = methodHandler.invoke(methodParams);

            if (isSuccessResult(result.getStatus())) {
                HttpResponseContext responseContext = new HttpResponseContext(gsonBuilder);

                result.toResponse(responseContext);

                sendResponse(
                        ctx,
                        request,
                        result.getStatus(),
                        Unpooled.copiedBuffer(responseContext.getContent().getBytes()),
                        responseContext.getHttpHeaders()
                );
            } else {
                sendResponse(ctx, request, result.getStatus(), null, null);
            }
        } else {
            sendResponse(ctx, request, restMethodResult.getStatus(), null, null);
        }
    }

    private boolean isSuccessResult(HttpResponseStatus status) {
        return status.equals(HttpResponseStatus.OK);
    }

    private void sendResponse(ChannelHandlerContext context,
                              FullHttpRequest request,
                              HttpResponseStatus status,
                              ByteBuf body,
                              HttpHeaders headers) {
        DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
                request.protocolVersion(),
                status,
                body == null ? Unpooled.buffer(0) : body,
                headers == null ? EmptyHttpHeaders.INSTANCE : headers,
                EmptyHttpHeaders.INSTANCE
        );

        context.channel().write(fullHttpResponse);
    }

    private RestMethodResult getAppropriateMethod(HttpMethod method, String uri) {
        for (RestBean handlerEntry : beanProcessor.getRestBeans()) {
            if (uri.startsWith(handlerEntry.getUrl())) {
                for (RestMethod methodHandlerEntry : handlerEntry.getRestMethods()) {
                    UriAttributesDecoder decoder = new UriAttributesDecoder(uri, handlerEntry.getUrl() + methodHandlerEntry.getUri());

                    if (decoder.isEqual()) {
                        if (methodHandlerEntry.probe(method)) {
                            return new RestMethodResult(methodHandlerEntry, decoder, HttpResponseStatus.OK);
                        } else {
                            return new RestMethodResult(null, null, HttpResponseStatus.METHOD_NOT_ALLOWED);
                        }
                    }
                }
            }
        }

        return new RestMethodResult(null, null, HttpResponseStatus.NOT_FOUND);
    }

    private HttpRequestContext createContext(QueryStringDecoder queryStringDecoder,
                                             FullHttpRequest request,
                                             UriAttributesDecoder uriAttributesDecoder,
                                             HttpPostRequestDecoder httpPostRequestDecoder) {
        return new HttpRequestContext(
                queryStringDecoder,
                request,
                gsonBuilder,
                uriAttributesDecoder,
                httpPostRequestDecoder
        );
    }

    private static class RestMethodResult {

        private RestMethod method;

        private UriAttributesDecoder uriAttributesDecoder;

        private HttpResponseStatus status;

        private RestMethodResult(RestMethod method, UriAttributesDecoder uriAttributesDecoder, HttpResponseStatus status) {
            this.method = method;
            this.uriAttributesDecoder = uriAttributesDecoder;
            this.status = status;
        }

        private RestMethod getMethod() {
            return method;
        }

        private UriAttributesDecoder getUriAttributesDecoder() {
            return uriAttributesDecoder;
        }

        private HttpResponseStatus getStatus() {
            return status;
        }
    }
}
