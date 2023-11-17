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

package com.aboni.sensors.hw;

import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.log.Log;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.data.Sample;

import java.io.FileInputStream;

public class CPUTemp {

    private static final int READ_THRESHOLD = 1999;

    private final byte[] bf = new byte[6];

    private Sample temp = new Sample(0, 0);

    private final boolean arm;

    private static final CPUTemp instance = new CPUTemp();

    private CPUTemp() {
        String name = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        arm = (arch.startsWith("arm") || name.toUpperCase().contains("LINUX"));
    }

    public static CPUTemp getInstance() {
        return instance;
    }

    private double read() {
        try {
            String sf = HWSettings.getProperty("thermal.zone", "/sys/class/thermal/thermal_zone0/temp");
            try (FileInputStream f = new FileInputStream(sf)) {
                int rr = f.read(bf);
                if (rr > 0) {
                    String s = new String(bf, 0, rr);
                    return Double.parseDouble(s) / 1000.0;
                }
            }
        } catch (Exception e) {
            ThingsFactory.getInstance(Log.class).debug("Cannot read cpu temperature {" + e.getMessage() + "}");
        }
        return 0;
    }

    public boolean isSupported() {
        return arm;
    }

    public double getTemp() {
        synchronized (this) {
            if (arm) {
                long t = System.currentTimeMillis();
                if (temp.getAge(t) > READ_THRESHOLD) {
                    temp = new Sample(t, read());
                }
            }
        }
        return temp.getValue();
    }
}
