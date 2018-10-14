package ru.saidgadjiev.prototype.core.service;

import ru.saidgadjiev.prototype.core.auth.SimpleUser;

/**
 * Created by said on 13.10.2018.
 */
public interface UserService {

    SimpleUser getUserByUsername(String username);

}
