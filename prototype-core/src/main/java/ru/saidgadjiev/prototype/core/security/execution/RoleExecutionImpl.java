package ru.saidgadjiev.prototype.core.security.execution;

import io.netty.handler.codec.http.HttpResponseStatus;
import ru.saidgadjiev.prototype.core.auth.User;

import java.util.Collections;
import java.util.Set;

/**
 * Created by said on 13.10.2018.
 */
public class RoleExecutionImpl implements Execution {

    private final Set<String> roles;

    public RoleExecutionImpl(Set<String> roles) {
        this.roles = roles;
    }

    @Override
    public HttpResponseStatus check(ExecutionContext executionContext) {
        User user = executionContext.getUser();

        if (user.getRoles().isEmpty()) {
            return HttpResponseStatus.UNAUTHORIZED;
        }
        boolean disjoint = Collections.disjoint(roles, user.getRoles());

        return disjoint ? HttpResponseStatus.FORBIDDEN : HttpResponseStatus.OK;
    }
}
