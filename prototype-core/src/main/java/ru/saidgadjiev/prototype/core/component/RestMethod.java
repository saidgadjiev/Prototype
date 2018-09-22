package ru.saidgadjiev.prototype.core.component;

import io.netty.handler.codec.http.*;
import ru.saidgadjiev.prototype.core.annotation.ResponseBody;
import ru.saidgadjiev.prototype.core.http.HttpResponse;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Created by said on 14.09.2018.
 */
public class RestMethod {

    private final Method restMethod;

    private final Object restClassInstance;

    private final String uri;

    private final HttpMethod method;

    private final Collection<RestParam> restParams;

    private final boolean isResponseBody;

    public RestMethod(Method restMethod, Object restClassInstance, HttpMethod method, String uri, Collection<RestParam> restParams) {
        this.restMethod = restMethod;
        this.restClassInstance = restClassInstance;
        this.uri = uri;
        this.method = method;
        this.restParams = restParams;
        isResponseBody = restMethod.isAnnotationPresent(ResponseBody.class);
    }

    public String getUri() {
        return uri;
    }

    public boolean probe(HttpMethod method) {
        return this.method == method;
    }

    public Collection<RestParam> getParams() {
        return restParams;
    }

    public RestResult invoke(Collection<Object> methodParams) {
        try {
            Object result = restMethod.invoke(restClassInstance, methodParams.toArray(new Object[methodParams.size()]));

            if (result instanceof HttpResponse) {
                return new RestResult(((HttpResponse) result).getStatus(), ((HttpResponse) result).getContent());
            }

            return new RestResult(HttpResponseStatus.OK, result);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public interface RestParam {

        default RequiredResult checkRequired(HttpRequestContext context) {
            return new RequiredResult(HttpResponseStatus.OK);
        }

        Object convert(HttpRequestContext requestContext);
    }

    public static class RequiredResult {

        private final HttpResponseStatus status;

        RequiredResult(HttpResponseStatus status) {
            this.status = status;
        }

        public HttpResponseStatus getStatus() {
            return status;
        }
    }

    public class RestResult {

        private final HttpResponseStatus status;

        private final Object content;

        public RestResult(HttpResponseStatus status, Object content) {
            this.status = status;
            this.content = content;
        }

        public void toResponse(HttpResponseContext responseContext) {
            if (isResponseBody) {
                responseContext.setContent(responseContext.getGsonBuilder().create().toJson(content));
            } else {
                responseContext.setContent(String.valueOf(content));
            }

            responseContext.addHeader(HttpHeaderNames.CONTENT_TYPE, responseContext.getContent().length());
        }

        public HttpResponseStatus getStatus() {
            return status;
        }
    }
}
