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

import com.aboni.nmea.router.utils.Tester;
import com.pi4j.io.gpio.*;

import java.io.PrintStream;

public class TestLed implements Tester.TestingProc {

    private static final GpioPinDigitalOutput[] pins = new GpioPinDigitalOutput[30];

    private static GpioPinDigitalOutput getPin(int i) {
        if (pins[i] == null) {
            GpioController gpio = GpioFactory.getInstance();
            pins[i] = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + i), "p" + i, PinState.LOW);
        }
        return pins[i];
    }

    @Override
    public boolean doIt(PrintStream out) {
        try {
            byte[] b =  new byte[256];
            if (System.in.read(b)>0) {
                String input = new String(b);
                String p = input.split(" ")[0].trim();
                String s = input.split(" ")[1].trim();

                out.println("Requested Pin " + p + " [" + s + "]");

                GpioPinDigitalOutput pin = getPin(Integer.parseInt(p));
                pin.setShutdownOptions(true, PinState.LOW);
                if ("1".equals(s)) {
                    pin.high();
                } else if ("0".equals(s)) {
                    pin.low();
                }
                out.println("Pin " + pin.getName() + " [" + pin.getState() + "]");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace(out);
            return false;
        }
    }

    @Override
    public boolean init(PrintStream out) {
        for (Pin rp: RaspiPin.allPins()) {
            Pin pp = RaspiPin.getPinByName(rp.getName());
            out.println("p " + rp.getName() + " " + (rp==pp));
        }
        return true;
    }

    public static void main(String[] args) {
        TestLed l = new TestLed();
        new Tester(0).start(l);
    }

}
