package ru.saidgadjiev.prototype.core.security.execution;

import ru.saidgadjiev.prototype.core.auth.User;

import java.util.Map;

/**
 * Created by said on 13.10.2018.
 */
public class ExecutionContext {

    private User user;

    private Map<String, Object> methodParams;

    public ExecutionContext(User user, Map<String, Object> methodParams) {
        this.user = user;
        this.methodParams = methodParams;
    }

    public User getUser() {
        return user;
    }

    public Object getParam(String name) {
        return methodParams.get(name);
    }
}
