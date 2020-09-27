package com.aboni.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class ConsoleLog implements Log {

    private static class MyFormatter extends Formatter {

        final DateFormat df;

        MyFormatter() {
            df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS");
        }

        @Override
        public String format(LogRecord record) {
            Date d = new Date(record.getMillis());
            String s = df.format(d) + " " + record.getLevel() + " " + record.getMessage() + "\n";
            if (record.getThrown()!=null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                s += sw.toString() + "\n";
            }
            return s;
        }
    }

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
        c.setFormatter(new MyFormatter());
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
