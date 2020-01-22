package com.aboni.sensors.hw;

public class RPIHelper {

    private static final boolean arm;

    public static boolean isRaspberry() {
        return arm;
    }

    static {
        arm = (System.getProperty("os.arch").startsWith("arm"));
    }

    private RPIHelper() {
    }
}
