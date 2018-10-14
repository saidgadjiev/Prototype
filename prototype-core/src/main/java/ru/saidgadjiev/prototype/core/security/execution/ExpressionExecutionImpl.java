package ru.saidgadjiev.prototype.core.security.execution;

import io.netty.handler.codec.http.HttpResponseStatus;
import ru.saidgadjiev.prototype.core.auth.User;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 13.10.2018.
 */
public class ExpressionExecutionImpl implements Execution {

    private final Object expressionBean;

    private final Method expressionMethod;

    private final String[] args;

    public ExpressionExecutionImpl(Object expressionBean, Method expressionMethod, String args[]) {
        this.expressionBean = expressionBean;
        this.expressionMethod = expressionMethod;
        this.args = args;
    }

    @Override
    public HttpResponseStatus check(ExecutionContext executionContext) throws Exception {
        Collection<Object> params = new ArrayList<>();

        for (String name: args) {
            params.add(executionContext.getParam(name));
        }

        boolean result = (boolean) expressionMethod.invoke(expressionBean, params.toArray(new Object[params.size()]));
        User user = executionContext.getUser();

        return result ? HttpResponseStatus.OK : user.isAnonymous() ? HttpResponseStatus.UNAUTHORIZED : HttpResponseStatus.FORBIDDEN;
    }
}
