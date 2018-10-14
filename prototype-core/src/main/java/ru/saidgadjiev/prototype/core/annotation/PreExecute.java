package ru.saidgadjiev.prototype.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by said on 13.10.2018.
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface PreExecute {

    Class<?> type();

    String method();

    String[] args();
}
