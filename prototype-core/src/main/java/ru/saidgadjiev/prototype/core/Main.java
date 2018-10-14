package ru.saidgadjiev.prototype.core;

import ru.saidgadjiev.prototype.core.server.PrototypeServer;
import ru.saidgadjiev.prototype.core.test.TestModule;

/**
 * Created by said on 13.10.2018.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        PrototypeServer server = new PrototypeServer.Builder(8080)
                .modules(new TestModule())
                .packages("ru")
                .build();

        server.run();
    }

}
