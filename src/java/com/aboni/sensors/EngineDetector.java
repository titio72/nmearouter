package com.aboni.sensors;

import com.aboni.sensors.hw.PinDetector;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
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
        Pin p = RaspiPin.getPinByName(HWSettings.getProperty("engine.pin", "GPIO 27"));
        pin = new PinDetector(p, false);
        ServerLog.getLogger().info("Engine Detection created {" + pin.getName() + "}");
    }

    public void refresh() {
        pin.refresh();
    }

    public boolean isEngineOn() {
        return pin.isPinOn();
    }
}
