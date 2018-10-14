package ru.saidgadjiev.prototype.core.http.session;

import ru.saidgadjiev.prototype.core.http.HttpSession;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by said on 13.10.2018.
 */
public class SessionCreatorImpl implements SessionCreator {

    private AtomicInteger uniqueGenerator = new AtomicInteger();

    @Override
    public HttpSession createSession() {
        String id = String.valueOf(uniqueGenerator.incrementAndGet());

        return new HttpSession(id);
    }
}
