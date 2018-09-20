package ru.saidgadjiev.prototype.core.test;

import ru.saidgadjiev.prototype.core.annotation.*;

/**
 * Created by said on 14.09.2018.
 */
@REST
public class RestTest {

    @POST("/")
    public String hello(@RequestBody POJO pojo) {
        return "Hello " + pojo.getName();
    }
}
