package ru.saidgadjiev.prototype.request.lexer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by said on 12.09.2018.
 */
public class HttpRequest {

    private Method method;

    private String uri;

    private String httpVersion;

    private String body;

    private Map<String, String> queryParams = new LinkedHashMap<String, String>();

    private Map<String, String> headers = new LinkedHashMap<String, String>();

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void addParam(String name, String value) {
        queryParams.put(name, value);
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public enum Method {
        GET
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method=" + method +
                ", uri='" + uri + '\'' +
                ", httpVersion='" + httpVersion + '\'' +
                ", body='" + body + '\'' +
                ", queryParams=" + queryParams +
                ", headers=" + headers +
                '}';
    }
}
