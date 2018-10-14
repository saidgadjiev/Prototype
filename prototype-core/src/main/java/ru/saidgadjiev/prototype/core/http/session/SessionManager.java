package ru.saidgadjiev.prototype.core.http.session;

import io.netty.handler.codec.http.HttpRequest;
import ru.saidgadjiev.prototype.core.http.HttpSession;

/**
 * Created by said on 13.10.2018.
 */
public interface SessionManager {

    HttpSession getSession(HttpRequest request, boolean create);

    void invalidate(HttpSession session);
}
