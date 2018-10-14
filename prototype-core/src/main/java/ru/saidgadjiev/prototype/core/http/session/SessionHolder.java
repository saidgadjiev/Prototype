package ru.saidgadjiev.prototype.core.http.session;

import ru.saidgadjiev.prototype.core.http.HttpSession;

/**
 * Created by said on 13.10.2018.
 */
public interface SessionHolder {

    void saveSession(String jSessionId, HttpSession session);

    HttpSession getSession(String jSessionId);

    void removeSession(String jSessionId);

}
