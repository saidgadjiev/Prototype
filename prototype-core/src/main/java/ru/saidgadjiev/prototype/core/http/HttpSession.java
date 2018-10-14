package ru.saidgadjiev.prototype.core.http;

import ru.saidgadjiev.prototype.core.http.session.SessionManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by said on 13.10.2018.
 */
public class HttpSession {

    private final String id;

    private SessionManager sessionManager;

    private Map<String, Object> sessionValues = new HashMap<>();

    public HttpSession(String id, SessionManager sessionManager) {
        this.id = id;
        this.sessionManager = sessionManager;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String name, Object value) {
        sessionValues.put(name, value);
    }

    public Object getAttribute(String name) {
        return sessionValues.get(name);
    }

    public void invalidate() {
        sessionManager.invalidate(this);
    }
}
