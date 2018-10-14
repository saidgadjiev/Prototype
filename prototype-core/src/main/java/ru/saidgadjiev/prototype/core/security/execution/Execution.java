package ru.saidgadjiev.prototype.core.security.execution;

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Created by said on 13.10.2018.
 */
public interface Execution {

    HttpResponseStatus check(ExecutionContext executionContext) throws Exception;
}
