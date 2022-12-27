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

import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.Tester;
import com.aboni.sensors.SensorVoltage;

import java.io.PrintStream;

public class TestVoltage {

    private static final String FORMAT = "%.3f";

    public static void main(String[] args) {

        new Tester(500).start(new Tester.TestingProc() {
            final SensorVoltage v = new SensorVoltage(0x48, ConsoleLog.getLogger());

            @Override
            public boolean doIt(PrintStream out) {
                try {
                    v.read();
                    out.format(FORMAT, v.getVoltage0());
                    out.format(FORMAT, v.getVoltage1());
                    out.format(FORMAT, v.getVoltage2());
                    out.format(FORMAT, v.getVoltage3());
                    out.println();
                    return true;
                } catch (Exception e) {
                    e.printStackTrace(out);
                    return false;
                }
            }

            @Override
            public boolean init(PrintStream out) {
                return true;
            }
        });
    }
}
