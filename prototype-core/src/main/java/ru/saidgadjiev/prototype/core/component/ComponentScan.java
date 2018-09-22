package ru.saidgadjiev.prototype.core.component;

import org.reflections.Reflections;
import ru.saidgadjiev.prototype.core.annotation.REST;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by said on 14.09.2018.
 */
public class ComponentScan {

    private final String basePackages;

    private Set<Class<?>> restClasses;

    public ComponentScan(String basePackages) {
        this.basePackages = basePackages;

        scan();
    }

    private void scan() {
        Reflections reflections = new Reflections(basePackages);

        restClasses = reflections.getTypesAnnotatedWith(REST.class);
    }

    public Set<Class<?>> getRestClasses() {
        return restClasses;
    }
}
