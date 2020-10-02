package com.aboni.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleLog implements Log {

    private final Logger lgConsole;

    private static final ConsoleLog logger = new ConsoleLog();

    public static ConsoleLog getLogger() {
        return logger;
    }

    private ConsoleLog() {
        lgConsole = Logger.getLogger("NMEAConsole");
        lgConsole.setLevel(Level.INFO);
        lgConsole.setUseParentHandlers(false);
        ConsoleHandler c = new ConsoleHandler();
        lgConsole.addHandler(c);
        c.setFormatter(new LogFormatter());
    }

    @Override
    public boolean isDebug() {
        return Level.FINEST == lgConsole.getLevel();
    }

    public void setDebug() {
        lgConsole.setLevel(Level.FINEST);
    }

    public void setError() {
        lgConsole.setLevel(Level.SEVERE);
    }

    public void setWarning() {
        lgConsole.setLevel(Level.WARNING);
    }

    public void setNone() {
        lgConsole.setLevel(Level.OFF);
    }

    public void setInfo() {
        lgConsole.setLevel(Level.INFO);
    }

    @Override
    public void error(String msg) {
        lgConsole.log(Level.SEVERE, msg);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        lgConsole.log(Level.SEVERE, msg, t);
    }

    @Override
    public void errorForceStacktrace(final String msg, final Throwable t) {
        lgConsole.log(Level.SEVERE, msg, t);
    }

    @Override
    public void warning(String msg) {
        lgConsole.log(Level.WARNING, msg);
    }

    @Override
    public void warning(String msg, Exception e) {
        lgConsole.warning(() -> String.format("{%s} error {%s}", msg, e.getMessage()));
    }

    @Override
    public void info(String msg) {
        lgConsole.log(Level.INFO, msg);
    }

    @Override
    public void infoFill(String msg) {
        lgConsole.log(Level.INFO, () -> fill(msg));
    }

    @Override
    public void debug(String msg) {
        lgConsole.log(Level.FINER, msg);
    }

    @Override
    public void console(String msg) {
        lgConsole.log(Level.INFO, msg);
    }

    private static final String FILLER = "--------------------------------------------------------------------------------";
    private static final String FILLER_LEFT = "---";

    private static String fill(String msg) {
        if (msg == null || msg.isEmpty()) return FILLER;
        return (FILLER_LEFT + " " + msg + " " + FILLER).substring(0, 80);
    }
}
