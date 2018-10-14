package ru.saidgadjiev.prototype.core.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import ru.saidgadjiev.prototype.core.security.bcrypt.BCryptPasswordEncoder;
import ru.saidgadjiev.prototype.core.security.password.PasswordEncoder;
import ru.saidgadjiev.prototype.core.service.AuthService;
import ru.saidgadjiev.prototype.core.service.AuthServiceImpl;

import java.util.Collection;
import java.util.Set;

/**
 * Created by said on 13.10.2018.
 */
public class PrototypeModule extends AbstractModule {

    private final Collection<Class<?>> restClasses;

    private final Collection<Class<?>> serviceClasses;

    private Set<Class<?>> expressionClasses;

    public PrototypeModule(Collection<Class<?>> restClasses, Collection<Class<?>> serviceClasses, Set<Class<?>> expressionClasses) {
        this.restClasses = restClasses;
        this.serviceClasses = serviceClasses;
        this.expressionClasses = expressionClasses;
    }

    @Override
    protected void configure() {
        for (Class<?> restClass: restClasses) {
            bind(restClass);
            
            for (Class<?> interfaceClass: restClass.getInterfaces()) {
                bind(interfaceClass).to((Class) restClass).in(Scopes.SINGLETON);
            }
        }
        for (Class<?> serviceClass: serviceClasses) {
            bind(serviceClass);

            for (Class<?> interfaceClass: serviceClass.getInterfaces()) {
                bind(interfaceClass).to((Class) serviceClass).in(Scopes.SINGLETON);
            }
        }
        for (Class<?> expressionClass: expressionClasses) {
            bind(expressionClass).in(Scopes.NO_SCOPE);
        }

        bind(PasswordEncoder.class).to(BCryptPasswordEncoder.class);
        bind(AuthService.class).to(AuthServiceImpl.class).in(Scopes.SINGLETON);
    }
}
