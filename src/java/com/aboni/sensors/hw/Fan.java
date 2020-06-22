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

import com.pi4j.io.gpio.*;

public class Fan {
    private GpioPinDigitalOutput pin;
    private boolean fanOn;

    public Fan() {
        if (RPIHelper.isRaspberry()) {
            GpioController gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "fan", PinState.LOW);
            pin.setShutdownOptions(true, PinState.LOW);
        }
    }

    public void switchFan(boolean on) {
        if (RPIHelper.isRaspberry()) {
            if (on) {
                pin.high();
            } else {
                pin.low();
            }
        }
        fanOn = on;
    }

    public boolean isFanOn() {
        return fanOn;
    }
}
