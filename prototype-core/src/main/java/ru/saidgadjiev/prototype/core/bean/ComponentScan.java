package ru.saidgadjiev.prototype.core.bean;

import org.reflections.Reflections;
import ru.saidgadjiev.prototype.core.annotation.Expression;
import ru.saidgadjiev.prototype.core.annotation.REST;
import ru.saidgadjiev.prototype.core.annotation.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by said on 14.09.2018.
 */
public class ComponentScan {

    private final Collection<String> packages;

    private Set<Class<?>> restClasses;

    private Set<Class<?>> serviceClasses;

    private Set<Class<?>> expressionClasses;

    public ComponentScan(Collection<String> packages) {
        this.packages = packages;

        scan();
    }

    private void scan() {
        Reflections reflections = new Reflections(packages);

        restClasses = reflections.getTypesAnnotatedWith(REST.class);
        serviceClasses = reflections.getTypesAnnotatedWith(Service.class);
        expressionClasses = reflections.getTypesAnnotatedWith(Expression.class);
    }

    public Set<Class<?>> getRestClasses() {
        return restClasses;
    }

    public Set<Class<?>> getServiceClasses() {
        return serviceClasses;
    }

    public Set<Class<?>> getExpressionClasses() {
        return expressionClasses;
    }
}
