package com.aboni.nmea.router.utils;

import java.util.function.Supplier;

public class NullLog implements Log {
    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public void error(String msg) {
        // null log: do nothing
    }

    @Override
    public void error(Supplier<String> msg) {
        // null log: do nothing
    }

    @Override
    public void error(String msg, Throwable t) {
        // null log: do nothing
    }

    @Override
    public void error(Supplier<String> supplier, Throwable t) {
        // null log: do nothing
    }

    @Override
    public void errorForceStacktrace(String msg, Throwable t) {
        // null log: do nothing
    }

    @Override
    public void errorForceStacktrace(Supplier<String> supplier, Throwable t) {
        // null log: do nothing
    }

    @Override
    public void warning(String msg) {
        // null log: do nothing
    }

    @Override
    public void warning(Supplier<String> msg) {
        // null log: do nothing
    }

    @Override
    public void warning(String msg, Exception e) {
        // null log: do nothing
    }

    @Override
    public void warning(Supplier<String> msg, Exception e) {
        // null log: do nothing
    }

    @Override
    public void info(String msg) {
        // null log: do nothing
    }

    @Override
    public void info(Supplier<String> msg) {
        // null log: do nothing
    }

    @Override
    public void infoFill(String msg) {
        // null log: do nothing
    }

    @Override
    public void debug(String msg) {
        // null log: do nothing
    }
}
