package ru.saidgadjiev.prototype.core;

import ru.saidgadjiev.prototype.core.component.ComponentScan;

/**
 * Created by said on 15.09.2018.
 */
public class Main {

    public static void main(String[] args) {
        ComponentScan componentScan = new ComponentScan();

        componentScan.scan("ru.saidgadjiev.prototype.core");
    }
}
