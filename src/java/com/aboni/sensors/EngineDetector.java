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

package com.aboni.sensors;

import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.log.Log;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.sensors.hw.PinDetector;
import com.aboni.log.LogStringBuilder;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class EngineDetector {

    private static EngineDetector instance;

    public static synchronized EngineDetector getInstance() {
        if (instance == null) instance = new EngineDetector();
        return instance;
    }

    private final PinDetector pin;

    private EngineDetector() {
        Pin p = RaspiPin.getPinByName(HWSettings.getProperty("engine.pin", "GPIO 0"));
        Log log = ThingsFactory.getInstance(Log.class);
        pin = new PinDetector(log, p, false);
        log.info(LogStringBuilder.start("Engine").wO("create")
                .wV("pin", pin.getName()).toString());
    }

    public void refresh() {
        pin.refresh();
    }

    public boolean isEngineOn() {
        return pin.isPinOn();
    }
}
