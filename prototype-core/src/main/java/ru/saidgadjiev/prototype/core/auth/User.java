package ru.saidgadjiev.prototype.core.auth;

import java.util.Collections;
import java.util.Set;

/**
 * Created by said on 13.10.2018.
 */
public interface User {

    default String getUsername() {
        return null;
    }

    default String getPassword() {
        return null;
    }

    default Set<String> getRoles() {
        return Collections.emptySet();
    }

    default boolean isAnonymous() {
        return true;
    }
}
