package ru.saidgadjiev.prototype.core.component;

import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import ru.saidgadjiev.prototype.core.codec.UriAttributesDecoder;

/**
 * Created by said on 22.09.2018.
 */
public class HttpRequestContext {

    private final QueryStringDecoder queryStringDecoder;

    private final FullHttpRequest request;

    private final GsonBuilder gsonBuilder;

    private final UriAttributesDecoder uriAttributesDecoder;

    private final HttpPostRequestDecoder httpPostRequestDecoder;

    public HttpRequestContext(QueryStringDecoder queryStringDecoder,
                              FullHttpRequest request,
                              GsonBuilder gsonBuilder,
                              UriAttributesDecoder uriAttributesDecoder,
                              HttpPostRequestDecoder httpPostRequestDecoder) {
        this.queryStringDecoder = queryStringDecoder;
        this.request = request;
        this.gsonBuilder = gsonBuilder;
        this.uriAttributesDecoder = uriAttributesDecoder;
        this.httpPostRequestDecoder = httpPostRequestDecoder;
    }

    public QueryStringDecoder getQueryStringDecoder() {
        return queryStringDecoder;
    }

    public FullHttpRequest getRequest() {
        return request;
    }

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }

    public UriAttributesDecoder getUriAttributesDecoder() {
        return uriAttributesDecoder;
    }

    public HttpPostRequestDecoder getHttpPostRequestDecoder() {
        return httpPostRequestDecoder;
    }
}
