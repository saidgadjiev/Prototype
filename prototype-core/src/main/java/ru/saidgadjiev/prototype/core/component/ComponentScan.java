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

    public Collection<RestClass> scan(String restBasePackage) {
        return getRestClasses(restBasePackage);
    }

    private Collection<RestClass> getRestClasses(final String packageName) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<?>> restClasses = reflections.getTypesAnnotatedWith(REST.class);

        return restClasses
                .stream()
                .map(aClass -> new RestClass(aClass, instantiate(aClass)))
                .collect(Collectors.toList());
    }

    private Object instantiate(Class<?> componentClass) {
        try {
            return componentClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
