package ru.saidgadjiev.prototype.core.test;

import ru.saidgadjiev.prototype.core.annotation.Expression;

/**
 * Created by said on 13.10.2018.
 */
@Expression
public class TestExpression {

    public boolean test(String name) {
        return "said".equals(name);
    }
}
