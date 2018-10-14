package ru.saidgadjiev.prototype.core.test;

import com.google.inject.Inject;
import ru.saidgadjiev.prototype.core.annotation.*;
import ru.saidgadjiev.prototype.core.http.FilePart;
import ru.saidgadjiev.prototype.core.http.HttpSession;

/**
 * Created by said on 14.09.2018.
 */
@REST
public class RestTest {

    private Test test;

    @Inject
    public RestTest(Test test) {
        this.test = test;
    }

    @GET("/")
    @PreExecute(type = TestExpression.class, method = "test", args = "arg0")
    @Role("ROLE_USER")
    public String hello(@RequestParam("name") String name, HttpSession session) {
        if (session.getAttribute("name") == null) {
            session.setAttribute("name", "said");
        }

        return "Hello " + session.getAttribute("name");
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

    @GET("/path/di/{name}")
    public String di(@PathParam("name") String name) {
        return test.hello(name);
    }
}
