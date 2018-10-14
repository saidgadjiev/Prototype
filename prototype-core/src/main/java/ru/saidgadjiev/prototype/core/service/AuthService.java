package ru.saidgadjiev.prototype.core.service;

import ru.saidgadjiev.prototype.core.auth.SimpleUser;
import ru.saidgadjiev.prototype.core.auth.User;
import ru.saidgadjiev.prototype.core.http.HttpSession;

/**
 * Created by said on 13.10.2018.
 */
public interface AuthService {

    void auth(String username, String password, HttpSession session);

    void logout(HttpSession session);

    User getAuthenticatedUser(HttpSession session);
}
