/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

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
