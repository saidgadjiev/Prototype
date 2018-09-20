package ru.saidgadjiev.prototype.core.annotation;

import java.lang.annotation.*;

/**
 * Created by said on 14.09.2018.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GET {

    String value() default "";
}
