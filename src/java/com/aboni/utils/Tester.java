package com.aboni.utils;

import com.aboni.misc.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tester {

    public interface TestingProc {
        boolean doIt(PrintStream out);
        boolean init(PrintStream out);
        void shutdown(PrintStream out);
    }

    private final AtomicBoolean goon;
    private final int period;

    public Tester(int period) {
        goon = new AtomicBoolean();
        this.period = period;
    }

    private PrintStream getOut() {
        return ServerLog.getConsoleOut();
    }

    public void start(TestingProc runnable) {
        if (runnable.init(getOut())) {
            goon.set(true);
            Thread t = new Thread(() -> {
                try {
                    while (goon.get()) {
                        if (runnable.doIt(getOut())) {
                            Utils.pause(period);
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, "Error", e);
                }
                runnable.shutdown(getOut());
            });
            t.setDaemon(true);
            t.start();

            try {
                System.in.read();
            } catch (IOException e) {
                // do nothing
            }
            goon.set(false);
        }
    }
}