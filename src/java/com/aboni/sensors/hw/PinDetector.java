package com.aboni.sensors.hw;

import com.aboni.utils.ServerLog;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinDetector {

    private GpioPinDigitalInput pin;
    private boolean pinOn;
    private final Set<PinListener> listener;
    private InternalPinListener pinListener = new InternalPinListener();
    private final String name;

    public interface PinListener {
        void engineStateEvent(boolean status);
    }

    private class InternalPinListener implements GpioPinListenerDigital {
        @Override
        public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            ServerLog.getLogger().info("PinDetector: State {" + pinOn + "}");
            pinOn = event.getState().isHigh();
            List<PinListener> listeners;
            synchronized (this) {
                listeners = new ArrayList<>(listener);
            }
            for (PinListener l : listeners) {
                try {
                    l.engineStateEvent(pinOn);
                } catch (Exception t) {
                    ServerLog.getLogger().error("Error notifying pin status", t);
                }
            }
        }
    }

    public PinDetector(Pin p, boolean active) {
        listener = new HashSet<>();
        name = "pin_" + p.getName();
        if (RPIHelper.isRaspberry()) {
            GpioController gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalInputPin(p, name, PinPullResistance.PULL_DOWN);
            pin.setShutdownOptions(true);
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
