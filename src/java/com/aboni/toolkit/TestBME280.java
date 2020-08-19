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

import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorPressureTemp;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestBME280 implements Tester.TestingProc {

    final SensorPressureTemp sp = new SensorPressureTemp(SensorPressureTemp.Sensor.BME280);

    @Override
    public boolean doIt(PrintStream out) {
        try {
            sp.read();
            out.format("P %.2fmb T %.2f°C H %.2f%%\r", sp.getPressureMB(), sp.getTemperatureCelsius(), sp.getHumidity());
            return true;
        } catch (Exception e) {
            e.printStackTrace(out);
            return false;
        }
    }

    @Override
    public boolean init(PrintStream out) {
        try {
            sp.init();
        } catch (SensorException e) {
            e.printStackTrace(out);
        }
        return true;
    }

    public static void main(String[] args) {
        new Tester(500).start(new TestBME280());
    }
}
