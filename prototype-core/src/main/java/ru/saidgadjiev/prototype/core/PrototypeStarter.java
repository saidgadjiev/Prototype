package ru.saidgadjiev.prototype.core;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import ru.saidgadjiev.prototype.core.server.PrototypeServer;
import ru.saidgadjiev.prototype.core.test.TestModule;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeStarter {

    public static void main(String[] args) throws Exception {
        PrototypeServer prototypeServer = new PrototypeServer(8080);

        prototypeServer.setRestBasePackage("ru.saidgadjiev.prototype.core.test");
        prototypeServer.setInjector(createInjector());

        prototypeServer.run();
    }

    private static Injector createInjector() {
        return Guice.createInjector(new TestModule());
    }
}
