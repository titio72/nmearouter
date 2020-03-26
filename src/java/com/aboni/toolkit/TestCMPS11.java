package com.aboni.toolkit;

import com.aboni.sensors.CMPS11CompassDataProvider;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestCMPS11 {

    static final CMPS11CompassDataProvider sp = new CMPS11CompassDataProvider();

    public static void main(String[] args) {

        new Tester(1000).start(new Tester.TestingProc() {

            @Override
            public boolean doIt(PrintStream out) {
                return handleChange(out);
            }

            @Override
            public boolean init(PrintStream out) {
                return initSensor(out);
            }
        });
    }

    static boolean initSensor(PrintStream out) {
        try {
            sp.init();
            return true;
        } catch (Exception e) {
            e.printStackTrace(out);
            return false;
        }
    }

    static boolean handleChange(PrintStream out) {
        try {
            double[] res = sp.read();
            out.format("H %.0fd\r", res[2]);
            return true;
        } catch (Exception e) {
            e.printStackTrace(out);
            return false;
        }
    }

}
