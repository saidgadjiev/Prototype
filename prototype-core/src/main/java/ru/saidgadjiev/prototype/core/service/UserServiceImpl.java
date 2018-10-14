package ru.saidgadjiev.prototype.core.service;

import com.google.inject.Inject;
import ru.saidgadjiev.prototype.core.annotation.Service;
import ru.saidgadjiev.prototype.core.auth.SimpleUser;
import ru.saidgadjiev.prototype.core.security.password.PasswordEncoder;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by said on 14.10.2018.
 */
@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;

    @Inject
    public UserServiceImpl(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SimpleUser getUserByUsername(String username) {
        if (username.equals("said")) {
            return new SimpleUser("said", passwordEncoder.encode("test"), new HashSet<>(Arrays.asList("ROLE_USER")));
        }

        return null;
    }
}
