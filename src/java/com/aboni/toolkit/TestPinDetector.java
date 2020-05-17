package com.aboni.toolkit;

import com.aboni.sensors.EngineDetector;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestPinDetector {

    static boolean s = false;
    static final EngineDetector e = EngineDetector.getInstance();

    public static void main(String[] args) {
        new Tester(500).start(new Tester.TestingProc() {

            @Override
            public boolean doIt(PrintStream out) {
                return traceEngineChanges(out);
            }

            @Override
            public boolean init(PrintStream out) {
                return true;
            }
        });
    }

    private static boolean traceEngineChanges(PrintStream out) {
        try {
            e.refresh();
            if (s != e.isEngineOn()) {
                s = e.isEngineOn();
                out.println(s);
            }
            return true;
        } catch (Exception ex) {
            ex.printStackTrace(out);
            return false;
        }
    }
}
