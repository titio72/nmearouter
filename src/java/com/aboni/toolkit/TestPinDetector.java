package com.aboni.toolkit;

import com.aboni.sensors.EngineDetector;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestPinDetector {

    public static void main(String[] args) {
        new Tester(500).start(new Tester.TestingProc() {

            EngineDetector e = EngineDetector.getInstance();
            boolean s = false;


            @Override
            public boolean doIt(PrintStream out) {
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

            @Override
            public boolean init(PrintStream out) {
                return true;
            }

            @Override
            public void shutdown(PrintStream out) {
                // nothing to bring down
            }
        });
    }
}
