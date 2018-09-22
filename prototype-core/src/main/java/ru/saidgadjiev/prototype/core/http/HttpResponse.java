package ru.saidgadjiev.prototype.core.http;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by said on 22.09.2018.
 */
public class HttpResponse<T> {

    private HttpResponseStatus status;

    private T content;

    public HttpResponse() {
    }

    public HttpResponse(HttpResponseStatus status, T content) {
        this.status = status;
        this.content = content;
    }

    public HttpResponseStatus getStatus() {
        return status;
    }

    public T getContent() {
        return content;
    }
}
