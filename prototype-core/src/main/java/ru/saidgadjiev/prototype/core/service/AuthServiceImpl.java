package ru.saidgadjiev.prototype.core.service;

import com.google.inject.Inject;
import ru.saidgadjiev.prototype.core.auth.AnonymousUser;
import ru.saidgadjiev.prototype.core.auth.SimpleUser;
import ru.saidgadjiev.prototype.core.auth.User;
import ru.saidgadjiev.prototype.core.http.HttpSession;
import ru.saidgadjiev.prototype.core.security.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 13.10.2018.
 */
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private Map<String, SimpleUser> authenticatedUserMap = new HashMap<>();

    @Inject
    public AuthServiceImpl(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void auth(String username, String password, HttpSession session) {
        SimpleUser simpleUser = userService.getUserByUsername(username);

        boolean result = passwordEncoder.matches(password, simpleUser.getPassword());

        if (!result) {
            //TODO: bad credentials
            return;
        }

        session.setAttribute("username", username);
        authenticatedUserMap.put(session.getId(), simpleUser);
    }

    @Override
    public void logout(HttpSession session) {
        if (session.getAttribute("username") == null) {
            //TODO: unauthorized
        } else {
            authenticatedUserMap.remove(session.getAttribute("username"));
        }

        session.invalidate();
    }

    @Override
    public User getAuthenticatedUser(HttpSession session) {
        User user = authenticatedUserMap.get(session.getId());

        if (user == null) {
            return new AnonymousUser();
        }

        return user;
    }
}
