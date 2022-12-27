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

import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import javax.validation.constraints.NotNull;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinDetector {

    private final Log log;
    private GpioPinDigitalInput pin;
    private boolean pinOn;
    private final Set<PinListener> listener;
    private final String name;

    public interface PinListener {
        void engineStateEvent(boolean status);
    }

    private class InternalPinListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            log.info(() -> LogStringBuilder.start("PinState").wO("state received").wV("state", pinOn).toString());
            pinOn = event.getState().isHigh();
            List<PinListener> listeners;
            synchronized (this) {
                listeners = new ArrayList<>(listener);
            }
            for (PinListener l : listeners) {
                try {
                    l.engineStateEvent(pinOn);
                } catch (Exception t) {
                    log.error(() -> LogStringBuilder.start("PinState").wO("state notification").wV("state", pinOn).toString(), t);
                }
            }
        }
    }

    public PinDetector(@NotNull Log log, Pin p, boolean active) {
        this.log = log;
        listener = new HashSet<>();
        name = "pin_" + p.getName();
        if (RPIHelper.isRaspberry()) {
            GpioController gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalInputPin(p, name, PinPullResistance.PULL_DOWN);
            pin.setShutdownOptions(true);
            InternalPinListener pinListener = new InternalPinListener();
            if (active) pin.addListener(pinListener);
        }
    }

    public void addListener(PinListener e) {
        synchronized (this) {
            listener.add(e);
        }
    }

    public void removeListener(PinListener e) {
        synchronized (this) {
            listener.remove(e);
        }
    }

    public boolean isPinOn() {
        synchronized (this) {
            return pinOn;
        }
    }

    public void refresh() {
        synchronized (this) {
            if (pin != null) {
                pinOn = pin.getState().isHigh();
            } else {
                readFromFile();
            }
        }
    }

    private void readFromFile() {
        try (FileReader is = new FileReader(name)) {
            char[] buffer = new char[16];
            int r = is.read(buffer);
            if (r>0) {
                String v = new String(buffer).trim();
                pinOn = 1 == (Integer.parseInt(v));
            } else {
                pinOn = false;
            }
        } catch (Exception e) {
            pinOn = false;
        }
    }

    public String getName() {
        return name;
    }
}
