package ru.saidgadjiev.prototype.request.lexer;

/**
 * Created by said on 12.09.2018.
 */
public class HttpRequest {

    private String method;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method='" + method + '\'' +
                '}';
    }
}
