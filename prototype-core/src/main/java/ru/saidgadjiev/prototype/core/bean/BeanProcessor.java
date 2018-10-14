package ru.saidgadjiev.prototype.core.bean;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by said on 21.09.2018.
 */
public class BeanProcessor {

    private Collection<RestBean> restBeans;

    private BeanFactory beanFactory;

    public BeanProcessor(ComponentScan componentScan, BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
        processRestBeans(componentScan.getRestClasses());
    }

    public Collection<RestBean> getRestBeans() {
        return restBeans;
    }

    private void processRestBeans(Set<Class<?>> restClasses) {
        restBeans = restClasses
                .stream()
                .map(restClass -> beanFactory.createRestBean(restClass))
                .collect(Collectors.toList());
    }
}
