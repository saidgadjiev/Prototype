package ru.saidgadjiev.prototype.core.server;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import ru.saidgadjiev.prototype.core.auth.User;
import ru.saidgadjiev.prototype.core.codec.UriAttributesDecoder;
import ru.saidgadjiev.prototype.core.bean.*;
import ru.saidgadjiev.prototype.core.http.HttpRequestContext;
import ru.saidgadjiev.prototype.core.http.HttpResponseContext;
import ru.saidgadjiev.prototype.core.http.session.SessionManager;
import ru.saidgadjiev.prototype.core.security.execution.ExecutionContext;
import ru.saidgadjiev.prototype.core.service.AuthService;

import java.util.*;


/**
 * Created by said on 12.09.2018.
 */
@ChannelHandler.Sharable
public class RequestHandler extends MessageToMessageDecoder<FullHttpRequest> {

    private final BeanProcessor beanProcessor;

    private GsonBuilder gsonBuilder;

    private AuthService authService;

    private SessionManager sessionManager;

    public RequestHandler(BeanProcessor beanProcessor,
                          GsonBuilder gsonBuilder,
                          SessionManager sessionManager) {
        this.beanProcessor = beanProcessor;
        this.gsonBuilder = gsonBuilder;
        this.sessionManager = sessionManager;
    }

    @Inject
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) throws Exception {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.uri());
        RestMethodResult restMethodResult = getAppropriateMethod(request.method(), queryStringDecoder.path());

        if (restMethodResult.getStatus().equals(HttpResponseStatus.OK)) {
            RestMethod methodHandler = restMethodResult.getMethod();
            UriAttributesDecoder uriAttributesDecoder = restMethodResult.getUriAttributesDecoder();

            Collection<RestMethod.RestParam> list = methodHandler.getParams();
            Map<String, Object> methodParams = new LinkedHashMap<>();
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
                        methodParams.put(param.getName(), param.convert(context));
                    } else {
                        sendResponse(ctx, request, requiredResult.getStatus(), null, null);

                        return;
                    }
                }
            } finally {
                if (context.getHttpPostRequestDecoder() != null) {
                    context.getHttpPostRequestDecoder().cleanFiles();
                }
            }

            HttpResponseStatus preExecutionsStatus = methodHandler.checkPreExecutions(createExecutionContext(request, methodParams));

            if (preExecutionsStatus == HttpResponseStatus.OK) {
                RestMethod.RestResult result = methodHandler.invoke(methodParams.values());

                if (isSuccessResult(result.getStatus())) {
                    HttpResponseContext responseContext = new HttpResponseContext(gsonBuilder, new DefaultHttpHeaders());

                    if (request.headers().contains(HttpHeaderNames.SET_COOKIE)) {
                        responseContext.addHeader(HttpHeaderNames.SET_COOKIE, request.headers().get(HttpHeaderNames.SET_COOKIE));
                    }
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
                sendResponse(ctx, request, preExecutionsStatus, null, null);
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

    private ExecutionContext createExecutionContext(FullHttpRequest request, Map<String, Object> params) {
        User authenticatedUser = authService.getAuthenticatedUser(sessionManager.getSession(request, false));

        return new ExecutionContext(authenticatedUser, params);
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
