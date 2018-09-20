package ru.saidgadjiev.prototype.core.component;

import com.google.gson.Gson;
import ru.saidgadjiev.prototype.core.annotation.GET;
import ru.saidgadjiev.prototype.core.annotation.POST;
import ru.saidgadjiev.prototype.core.annotation.RequestBody;
import ru.saidgadjiev.prototype.core.annotation.RequestParam;
import ru.saidgadjiev.prototype.http.HttpRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 14.09.2018.
 */
public class RestMethod {

    private final Method restMethod;

    private final Object restClassInstance;

    private final String uri;

    private final HttpRequest.Method method;

    private final Collection<RestParam> restParams;

    public RestMethod(Method restMethod, Object restClassInstance) {
        this.restMethod = restMethod;
        this.restClassInstance = restClassInstance;

        uri = getMethodUri();
        restParams = getRestParams();
        method = getRestMethod();
    }

    public String getUri() {
        return uri;
    }

    public boolean probe(HttpRequest.Method method) {
        return this.method == method;
    }

    public Collection<RestParam> getParams() {
        return restParams;
    }

    public RestResult invoke(Collection<Object> methodParams) {
        try {
            Object result = restMethod.invoke(restClassInstance, methodParams.toArray(new Object[methodParams.size()]));

            return new RestResult() {
                @Override
                public Object toBody() {
                    return String.valueOf(result);
                }

                @Override
                public int getLength() {
                    return String.valueOf(result).length();
                }
            };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String getMethodUri() {
        if (restMethod.isAnnotationPresent(GET.class)) {
            GET get = restMethod.getAnnotation(GET.class);

            return get.value();
        }
        if (restMethod.isAnnotationPresent(POST.class)) {
            POST post = restMethod.getAnnotation(POST.class);

            return post.value();
        }

        return null;
    }

    private HttpRequest.Method getRestMethod() {
        if (restMethod.isAnnotationPresent(GET.class)) {
            return HttpRequest.Method.GET;
        }
        if (restMethod.isAnnotationPresent(POST.class)) {
            return HttpRequest.Method.POST;
        }

        return null;
    }

    private Collection<RestParam> getRestParams() {
        Collection<RestParam> restParams = new ArrayList<>();

        for (Parameter parameter : restMethod.getParameters()) {
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);

                restParams.add(request -> request.getParam(requestParam.value()));
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                restParams.add(request -> new Gson().fromJson(request.getBody(), parameter.getType()));
            }
        }

        return restParams;
    }

    public interface RestParam {

        Object parse(HttpRequest request);
    }

    public interface RestResult {

        Object toBody();

        int getLength();
    }
}
