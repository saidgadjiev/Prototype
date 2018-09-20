package ru.saidgadjiev.prototype.core.component;

import ru.saidgadjiev.prototype.core.annotation.GET;
import ru.saidgadjiev.prototype.core.annotation.POST;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by said on 14.09.2018.
 */
public class RestClass {

    private final Class<?> restClass;

    private final Object restClassInstance;

    private final Collection<RestMethod> restMethods;

    public RestClass(Class<?> restClass, Object restClassInstance) {
        this.restClass = restClass;
        this.restClassInstance = restClassInstance;

        restMethods = resolveRestMethods();
    }

    public String getUrl() {
        return "";
    }

    public Collection<RestMethod> getRestMethods() {
        return restMethods;
    }

    private Collection<RestMethod> resolveRestMethods() {
        Collection<RestMethod> restMethods = new ArrayList<>();

        for (Method method: restClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(GET.class) || method.isAnnotationPresent(POST.class)) {
                restMethods.add(new RestMethod(method, restClassInstance));
            }
        }

        return restMethods;
    }
}
