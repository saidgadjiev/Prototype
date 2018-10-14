package ru.saidgadjiev.prototype.core.bean;

import java.util.*;

/**
 * Created by said on 14.09.2018.
 */
public class RestBean {

    private final Collection<RestMethod> restMethods;

    public RestBean(Collection<RestMethod> restMethods) {
        this.restMethods = restMethods;
    }

    public String getUrl() {
        return "";
    }

    public Collection<RestMethod> getRestMethods() {
        return restMethods;
    }
}
