package ru.saidgadjiev.prototype.core.test;

import com.google.inject.AbstractModule;

/**
 * Created by said on 23.09.2018.
 */
public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Test.class).to(TestImpl.class);
    }
}
