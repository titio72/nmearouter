package com.aboni.sensors.hw;

public class RPIHelper {

    private static final boolean ARM;

    public static boolean isRaspberry() {
        return ARM;
    }

    static {
        ARM = (System.getProperty("os.arch").startsWith("arm"));
    }

    private RPIHelper() {
    }
}
