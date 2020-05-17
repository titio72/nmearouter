package com.aboni.utils;

public interface Log {

    void error(String msg);

    void error(String msg, Throwable t);

    void errorForceStacktrace(String msg, Throwable t);

    void warning(String msg);

    void info(String msg);

    void infoFill(String msg);

    void debug(String msg);

    void console(String msg);

}