package ru.saidgadjiev.prototype.core.bean;

import com.google.inject.Injector;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.multipart.MixedFileUpload;
import ru.saidgadjiev.prototype.core.annotation.*;
import ru.saidgadjiev.prototype.core.http.FilePart;
import ru.saidgadjiev.prototype.core.http.HttpRequestContext;
import ru.saidgadjiev.prototype.core.http.HttpSession;
import ru.saidgadjiev.prototype.core.http.session.SessionManager;
import ru.saidgadjiev.prototype.core.security.execution.Execution;
import ru.saidgadjiev.prototype.core.security.execution.ExpressionExecutionImpl;
import ru.saidgadjiev.prototype.core.security.execution.RoleExecutionImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by said on 21.09.2018.
 */
public class BeanFactory {

    private final Injector injector;

    private SessionManager sessionManager;

    public BeanFactory(Injector injector, SessionManager sessionManager) {
        this.injector = injector;
        this.sessionManager = sessionManager;
    }

    public RestBean createRestBean(Class<?> beanClass) {
        try {
            return new RestBean(resolveRestMethods(beanClass, instantiate(beanClass)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object instantiate(Class<?> beanClass) throws Exception {
        if (injector != null) {
            return injector.getInstance(beanClass);
        } else {
            return beanClass.newInstance();
        }
    }

    private Collection<RestMethod> resolveRestMethods(Class<?> beanClass, Object beanInstance) {
        Collection<RestMethod> restMethods = new ArrayList<>();

        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class)) {
                restMethods.add(
                        new RestMethod(
                                method,
                                beanInstance,
                                getRestMethod(method),
                                getMethodUri(method),
                                getRestParams(method),
                                getPreExecutions(method)
                        )
                );
            }
        }

        return restMethods;
    }

    private Collection<Execution> getPreExecutions(Method restMethod) {
        Collection<Execution> preExecutions = new ArrayList<>();

        if (restMethod.isAnnotationPresent(Role.class)) {
            Role role = restMethod.getAnnotation(Role.class);

            preExecutions.add(new RoleExecutionImpl(new HashSet<>(Arrays.asList(role.value()))));
        }
        if (restMethod.isAnnotationPresent(PreExecute.class)) {
            PreExecute preExecute = restMethod.getAnnotation(PreExecute.class);

            Object preExecutionBean = injector.getInstance(preExecute.type());

            try {
                preExecutions.add(
                        new ExpressionExecutionImpl(
                                preExecutionBean,
                                getPreExecutionMethod(preExecute.type(), preExecute.method(), preExecute.args()),
                                preExecute.args())
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        return preExecutions;
    }

    private Method getPreExecutionMethod(Class<?> type, String methodName, String[] args) throws NoSuchMethodException {
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }

        throw new NoSuchMethodException(type.getName() + "." + methodName + "(" + Arrays.toString(args) + ")");
    }

    private String getMethodUri(Method restMethod) {
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

    private HttpMethod getRestMethod(Method restMethod) {
        if (restMethod.isAnnotationPresent(GET.class)) {
            return HttpMethod.GET;
        }
        if (restMethod.isAnnotationPresent(POST.class)) {
            return HttpMethod.POST;
        }

        return null;
    }

    private Collection<RestMethod.RestParam> getRestParams(Method restMethod) {
        Collection<RestMethod.RestParam> restParams = new ArrayList<>();

        for (Parameter parameter : restMethod.getParameters()) {
            if (parameter.getType() == HttpSession.class) {
                restParams.add(new HttpSessionParam(sessionManager, parameter.getName()));
            } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);

                restParams.add(new RequestParamImpl(requestParam.value(), requestParam.required(), parameter.getName()));
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);

                restParams.add(new RequestBodyImpl(parameter.getType(), requestBody.required(), parameter.getName()));
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                PathParam requestParam = parameter.getAnnotation(PathParam.class);

                restParams.add(new PathParamImpl(requestParam.value(), parameter.getType(), parameter.getName()));
            } else if (parameter.isAnnotationPresent(MultipartFile.class)) {
                MultipartFile multipartFile = parameter.getAnnotation(MultipartFile.class);

                restParams.add(new MultipartFileImpl(multipartFile.value(), multipartFile.required(), parameter.getName()));
            } else if (parameter.isAnnotationPresent(MultipartBody.class)) {
                MultipartBody multipartBody = parameter.getAnnotation(MultipartBody.class);

                restParams.add(new MultipartBodyImpl(multipartBody.value(), parameter.getType(), multipartBody.required(), parameter.getName()));
            } else if (parameter.isAnnotationPresent(MultipartBody.class)) {
                MultipartParam multipartParam = parameter.getAnnotation(MultipartParam.class);

                restParams.add(new MultipartParamImpl(multipartParam.value(), parameter.getType(), multipartParam.required(), parameter.getName()));
            }
        }

        return restParams;
    }

    private static Object parsePathParam(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        }
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        }

        return null;
    }

    private static abstract class AbstractRequestParam implements RestMethod.RestParam {

        private String name;

        public AbstractRequestParam(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final class RequestParamImpl extends AbstractRequestParam {

        private final String name;

        private final boolean required;

        private RequestParamImpl(String name, boolean required, String paramName) {
            super(paramName);
            this.name = name;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            List<String> params = requestContext.getQueryStringDecoder().parameters().get(name);

            return params.isEmpty() ? null : params.iterator().next();
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            if (required) {
                if (context.getQueryStringDecoder().parameters().containsKey(name)) {
                    return new RestMethod.RequiredResult(HttpResponseStatus.OK);
                } else {
                    return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
                }
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            }
        }
    }

    private static final class RequestBodyImpl extends AbstractRequestParam {

        private final Class<?> type;

        private boolean required;

        private RequestBodyImpl(Class<?> type, boolean required, String paramName) {
            super(paramName);
            this.type = type;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            String json = requestContext.getRequest().content().toString(Charset.defaultCharset());

            return requestContext.getGsonBuilder().create().fromJson(json, type);
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            if (required) {
                String json = context.getRequest().content().toString(Charset.defaultCharset());

                if (json != null && json.isEmpty()) {
                    return new RestMethod.RequiredResult(HttpResponseStatus.OK);
                } else {
                    return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
                }
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            }
        }
    }

    private static final class PathParamImpl extends AbstractRequestParam {

        private final Class<?> type;

        private final String name;

        private PathParamImpl(String name, Class<?> type, String paramName) {
            super(paramName);
            this.type = type;
            this.name = name;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            return parsePathParam(requestContext.getUriAttributesDecoder().getAttr(name), type);
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            String attr = context.getUriAttributesDecoder().getAttr(name);

            if (attr != null && !attr.isEmpty()) {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
            }
        }
    }

    private static class MultipartFileImpl extends AbstractRequestParam {

        private final String name;

        private boolean required;

        private MultipartFileImpl(String name, boolean required, String paramName) {
            super(paramName);
            this.name = name;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            InterfaceHttpData httpData = requestContext.getHttpPostRequestDecoder().getBodyHttpData(name);

            if (httpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                MixedFileUpload fileUpload = (MixedFileUpload) httpData;

                return new FilePart(fileUpload.getFilename()) {

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(fileUpload.getFile());
                    }
                };
            }

            return null;
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            if (required) {
                InterfaceHttpData httpData = context.getHttpPostRequestDecoder().getBodyHttpData(name);

                if (httpData != null && httpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.FileUpload)) {
                    return new RestMethod.RequiredResult(HttpResponseStatus.OK);
                } else {
                    return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
                }
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            }
        }

    }

    private static class MultipartBodyImpl extends AbstractRequestParam {

        private final String name;

        private final Class<?> type;

        private boolean required;

        private MultipartBodyImpl(String name, Class<?> type, boolean required, String paramName) {
            super(paramName);
            this.name = name;
            this.type = type;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            InterfaceHttpData httpData = requestContext.getHttpPostRequestDecoder().getBodyHttpData(name);

            MemoryAttribute attribute = (MemoryAttribute) httpData;

            return requestContext.getGsonBuilder().create().fromJson(attribute.getValue(), type);
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            if (required) {
                InterfaceHttpData httpData = context.getHttpPostRequestDecoder().getBodyHttpData(name);

                if (httpData != null && httpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    return new RestMethod.RequiredResult(HttpResponseStatus.OK);
                } else {
                    return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
                }
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            }
        }
    }

    private static class MultipartParamImpl extends AbstractRequestParam {

        private final String name;

        private final Class<?> type;

        private boolean required;

        private MultipartParamImpl(String name, Class<?> type, boolean required, String paramName) {
            super(paramName);
            this.name = name;
            this.type = type;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            InterfaceHttpData httpData = requestContext.getHttpPostRequestDecoder().getBodyHttpData(name);

            MemoryAttribute attribute = (MemoryAttribute) httpData;

            return requestContext.getGsonBuilder().create().fromJson(attribute.getValue(), type);
        }

        @Override
        public RestMethod.RequiredResult checkRequired(HttpRequestContext context) {
            if (required) {
                InterfaceHttpData httpData = context.getHttpPostRequestDecoder().getBodyHttpData(name);

                if (httpData != null && httpData.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute)) {
                    return new RestMethod.RequiredResult(HttpResponseStatus.OK);
                } else {
                    return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
                }
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            }
        }
    }

    private class HttpSessionParam extends AbstractRequestParam {

        private SessionManager sessionManager;

        public HttpSessionParam(SessionManager sessionManager, String name) {
            super(name);
            this.sessionManager = sessionManager;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            return sessionManager.getSession(requestContext.getRequest(), true);
        }
    }
}
