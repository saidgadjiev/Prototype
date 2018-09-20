package ru.saidgadjiev.prototype.core;

import ru.saidgadjiev.prototype.core.server.PrototypeServer;

/**
 * Created by said on 12.09.2018.
 */
public class PrototypeStarter {

    public static void main(String[] args) throws Exception {
        PrototypeServer prototypeServer = new PrototypeServer(8080);

        prototypeServer.setRestBasePackage("ru.saidgadjiev.prototype.core.test");

        prototypeServer.run();
    }
}
