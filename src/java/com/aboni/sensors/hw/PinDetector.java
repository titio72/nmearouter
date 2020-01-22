package com.aboni.sensors.hw;

import com.aboni.utils.ServerLog;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PinDetector {

    private GpioPinDigitalInput pin;
    private boolean pinOn;
    private final Set<PinListener> listener;
    private InternalPinListener pinListener = new InternalPinListener();

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
                } catch (Throwable t) {
                    ServerLog.getLogger().error("Error notifying pin status", t);
                }
            }
        }
    }

    public PinDetector(Pin p) {
        listener = new HashSet<>();
        if (RPIHelper.isRaspberry()) {
            GpioController gpio = GpioFactory.getInstance();
            pin = gpio.provisionDigitalInputPin(p, "pin_" + p.getName(), PinPullResistance.PULL_DOWN);
            pin.setShutdownOptions(true);
            pin.addListener(pinListener);
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
        return pinOn;
    }

    public static void main(String args[]) {
        PinDetector e = new PinDetector(RaspiPin.GPIO_27);
        e.addListener((boolean b) -> {
            System.out.println("Engine: " + b);
        });
    }
}
