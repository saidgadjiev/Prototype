package ru.saidgadjiev.prototype.core.auth;

import java.util.Collection;
import java.util.Set;

/**
 * Created by said on 13.10.2018.
 */
public class SimpleUser implements User {

    private final String username;

    private final String password;

    private final Set<String> roles;

    public SimpleUser(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Set<String> getRoles() {
        return roles;
    }
}
