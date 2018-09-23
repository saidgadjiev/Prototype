package ru.saidgadjiev.prototype.core.test;

/**
 * Created by said on 23.09.2018.
 */
public class TestImpl implements Test {
    @Override
    public String hello(String name) {
        return "Hello " + name;
    }
}
