package ru.saidgadjiev.prototype.core;

import com.google.inject.Injector;
import ru.saidgadjiev.prototype.core.server.PrototypeServer;

import java.util.Objects;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeStarter {

    private final int port;

    public PrototypeStarter(int port) {
        this.port = port;
    }

    public void start(String basePackage, Injector injector) throws Exception {
        Objects.requireNonNull(basePackage);
        Objects.requireNonNull(injector);
        PrototypeServer prototypeServer = new PrototypeServer(port);

        prototypeServer.setRestBasePackage(basePackage);
        prototypeServer.setInjector(injector);

        prototypeServer.run();
    }
}
