package ru.saidgadjiev.prototype.core.controller;

import com.google.inject.Inject;
import ru.saidgadjiev.prototype.core.annotation.POST;
import ru.saidgadjiev.prototype.core.annotation.REST;
import ru.saidgadjiev.prototype.core.annotation.RequestParam;
import ru.saidgadjiev.prototype.core.http.HttpSession;
import ru.saidgadjiev.prototype.core.service.AuthService;

/**
 * Created by said on 13.10.2018.
 */
@REST
public class AuthController {

    private final AuthService authService;

    @Inject
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @POST("/login")
    public void login(
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "password", required = true) String password,
            HttpSession session) {
        authService.auth(username, password, session);
    }

    @POST("/logout")
    public void logout(HttpSession session) {
        authService.logout(session);
    }
}
