package ru.saidgadjiev.prototype.core.test;

import ru.saidgadjiev.prototype.core.annotation.*;
import ru.saidgadjiev.prototype.core.http.FilePart;

/**
 * Created by said on 14.09.2018.
 */
@REST
public class RestTest {

    @GET("/")
    public String hello(@RequestParam("name") String name) {
        return "Hello " + name;
    }

    @POST("/pojo")
    public String pojo(@RequestBody POJO pojo) {
        return "Hello pojo " + pojo.getName();
    }

    @GET("/path/{id}")
    public String pathVar(@PathParam("id") int id) {
        return "Hello " + id;
    }

    @POST("/path/multipart")
    public String multipart(@MultipartFile("file") FilePart filePart) {
        return "";
    }
}
