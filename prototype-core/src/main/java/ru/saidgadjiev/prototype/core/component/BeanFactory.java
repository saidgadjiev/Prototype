package ru.saidgadjiev.prototype.core.component;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.handler.codec.http.multipart.MixedFileUpload;
import ru.saidgadjiev.prototype.core.annotation.*;
import ru.saidgadjiev.prototype.core.http.FilePart;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 21.09.2018.
 */
public class BeanFactory {

    public RestBean createRestBean(Class<?> beanClass) {
        try {
            return new RestBean(resolveRestMethods(beanClass, instantiate(beanClass)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Object instantiate(Class<?> beanClass) {
        try {
            return beanClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Collection<RestMethod> resolveRestMethods(Class<?> beanClass, Object beanInstance) {
        Collection<RestMethod> restMethods = new ArrayList<>();

        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class)) {
                restMethods.add(new RestMethod(method, beanInstance, getRestMethod(method), getMethodUri(method), getRestParams(method)));
            }
        }

        return restMethods;
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
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);

                restParams.add(new RequestParamImpl(requestParam.value(), requestParam.required()));
            } else if (parameter.isAnnotationPresent(RequestBody.class)) {
                RequestBody requestBody = parameter.getAnnotation(RequestBody.class);

                restParams.add(new RequestBodyImpl(parameter.getType(), requestBody.required()));
            } else if (parameter.isAnnotationPresent(PathParam.class)) {
                PathParam requestParam = parameter.getAnnotation(PathParam.class);

                restParams.add(new PathParamImpl(requestParam.value(), parameter.getType()));
            } else if (parameter.isAnnotationPresent(MultipartFile.class)) {
                MultipartFile multipartFile = parameter.getAnnotation(MultipartFile.class);

                restParams.add(new MultipartFileImpl(multipartFile.value(), multipartFile.required()));
            } else if (parameter.isAnnotationPresent(MultipartBody.class)) {
                MultipartBody multipartBody = parameter.getAnnotation(MultipartBody.class);

                restParams.add(new MultipartBodyImpl(multipartBody.value(), parameter.getType(), multipartBody.required()));
            } else if (parameter.isAnnotationPresent(MultipartBody.class)) {
                MultipartParam multipartParam = parameter.getAnnotation(MultipartParam.class);

                restParams.add(new MultipartParamImpl(multipartParam.value(), parameter.getType(), multipartParam.required()));
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

    private static final class RequestParamImpl implements RestMethod.RestParam {

        private final String name;

        private final boolean required;

        private RequestParamImpl(String name, boolean required) {
            this.name = name;
            this.required = required;
        }

        @Override
        public Object convert(HttpRequestContext requestContext) {
            return requestContext.getQueryStringDecoder().parameters().get(name);
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

    private static final class RequestBodyImpl implements RestMethod.RestParam {

        private final Class<?> type;

        private boolean required;

        private RequestBodyImpl(Class<?> type, boolean required) {
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

    private static final class PathParamImpl implements RestMethod.RestParam {

        private final Class<?> type;

        private final String name;

        private PathParamImpl(String name, Class<?> type) {
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

            if (attr != null && attr.isEmpty()) {
                return new RestMethod.RequiredResult(HttpResponseStatus.OK);
            } else {
                return new RestMethod.RequiredResult(HttpResponseStatus.BAD_REQUEST);
            }
        }
    }

    private static class MultipartFileImpl implements RestMethod.RestParam {

        private final String name;

        private boolean required;

        private MultipartFileImpl(String name, boolean required) {
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

    private static class MultipartBodyImpl implements RestMethod.RestParam {

        private final String name;

        private final Class<?> type;

        private boolean required;

        private MultipartBodyImpl(String name, Class<?> type, boolean required) {
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

    private static class MultipartParamImpl implements RestMethod.RestParam {

        private final String name;

        private final Class<?> type;

        private boolean required;

        private MultipartParamImpl(String name, Class<?> type, boolean required) {
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

}
