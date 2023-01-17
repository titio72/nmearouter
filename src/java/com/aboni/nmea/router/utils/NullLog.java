package com.aboni.nmea.router.utils;

import java.util.function.Supplier;

public class NullLog implements Log {
    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void error(Supplier<String> msg) {

    }

    @Override
    public void error(String msg, Throwable t) {

    }

    @Override
    public void error(Supplier<String> supplier, Throwable t) {

    }

    @Override
    public void errorForceStacktrace(String msg, Throwable t) {

    }

    @Override
    public void errorForceStacktrace(Supplier<String> supplier, Throwable t) {

    }

    @Override
    public void warning(String msg) {

    }

    @Override
    public void warning(Supplier<String> msg) {

    }

    @Override
    public void warning(String msg, Exception e) {

    }

    @Override
    public void warning(Supplier<String> msg, Exception e) {

    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(Supplier<String> msg) {

    }

    @Override
    public void infoFill(String msg) {

    }

    @Override
    public void debug(String msg) {

    }
}
