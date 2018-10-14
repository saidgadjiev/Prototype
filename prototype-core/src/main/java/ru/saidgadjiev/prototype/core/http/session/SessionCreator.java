package ru.saidgadjiev.prototype.core.http.session;

import ru.saidgadjiev.prototype.core.http.HttpSession;

/**
 * Created by said on 13.10.2018.
 */
public interface SessionCreator {

    HttpSession createSession();
}
