package ru.saidgadjiev.prototype.core.http.session;

import ru.saidgadjiev.prototype.core.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 13.10.2018.
 */
public class SessionHolderImpl implements SessionHolder {

    private Map<String, HttpSession> sessionMap = new HashMap<>();

    @Override
    public void saveSession(String jSessionId, HttpSession session) {
        sessionMap.put(jSessionId, session);
    }

    @Override
    public HttpSession getSession(String jSessionId) {
        return sessionMap.get(jSessionId);
    }

    @Override
    public void removeSession(String jSessionId) {
        if (jSessionId != null) {
            sessionMap.remove(jSessionId);
        }
    }
}
