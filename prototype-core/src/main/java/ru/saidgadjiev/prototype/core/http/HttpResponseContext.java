package ru.saidgadjiev.prototype.core.http;

import com.google.gson.GsonBuilder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by said on 22.09.2018.
 */
public class HttpResponseContext {

    private String content;

    private HttpHeaders httpHeaders;

    private final GsonBuilder gsonBuilder;

    public HttpResponseContext(GsonBuilder gsonBuilder, HttpHeaders httpHeaders) {
        this.gsonBuilder = gsonBuilder;
        this.httpHeaders = httpHeaders;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void addHeader(CharSequence name, Object value) {
        httpHeaders.add(name, value);
    }

    public GsonBuilder getGsonBuilder() {
        return gsonBuilder;
    }
}
