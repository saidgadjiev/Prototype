package ru.saidgadjiev.prototype.core.http.session;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import ru.saidgadjiev.prototype.core.http.HttpSession;

import java.util.Optional;
import java.util.Set;

/**
 * Created by said on 13.10.2018.
 */
public class SessionManagerImpl implements SessionManager {

    private final SessionCreator sessionCreator;

    private final SessionHolder sessionHolder;

    public SessionManagerImpl(SessionCreator sessionCreator, SessionHolder sessionHolder) {
        this.sessionCreator = sessionCreator;
        this.sessionHolder = sessionHolder;
    }

    @Override
    public HttpSession getSession(HttpRequest request, boolean create) {
        String cookieHeader = request.headers().get(HttpHeaderNames.COOKIE);

        if (cookieHeader == null) {
            if (create) {
                return handleNewSession(request);
            }

            return null;
        }

        Set<Cookie> cookies = ServerCookieDecoder.LAX.decode(cookieHeader);
        Optional<Cookie> jSessionId = cookies.stream().filter(cookie -> cookie.name().equals("JSESSIONID")).findAny();

        if (jSessionId.isPresent()) {
            HttpSession session = sessionHolder.getSession(jSessionId.get().value());

            if (session == null) {
                return handleNewSession(request);
            }

            return session;
        } else if (create) {
            return handleNewSession(request);
        }

        return null;
    }

    @Override
    public void invalidate(HttpSession session) {
        sessionHolder.removeSession(session.getId());
    }

    private HttpSession handleNewSession(HttpRequest request) {
        HttpSession session = sessionCreator.createSession();

        request.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.LAX.encode("JSESSIONID", session.getId()));

        sessionHolder.saveSession(session.getId(), session);

        return session;
    }
}
