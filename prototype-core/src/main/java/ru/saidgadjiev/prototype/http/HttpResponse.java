package ru.saidgadjiev.prototype.http;

import ru.saidgadjiev.prototype.core.component.RestMethod;

/**
 * Created by said on 15.09.2018.
 */
public class HttpResponse {



    private final HttpStatus status;

    private final RestMethod.RestResult body;

    public HttpResponse(HttpStatus status, RestMethod.RestResult body) {
        this.status = status;
        this.body = body;
    }

    public String getHttpVersion() {
        return "HTTP/1.1";
    }

    public HttpStatus getStatus() {
        return status;
    }

    public RestMethod.RestResult getBody() {
        return body;
    }
}
