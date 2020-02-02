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
        Pin p = RaspiPin.getPinByName(HWSettings.getProperty("engine.pin", "GPIO 24"));
        pin = new PinDetector(p, false);
        ServerLog.getLogger().info("Engine Detection created {" + pin.getName() + "}");
    }

    public void refresh() {
        pin.refresh();
    }

    public boolean isEngineOn() {
        return pin.isPinOn();
    }

    public static void main(String[] args) {
            try {
	                EngineDetector e = EngineDetector.getInstance();
	                boolean s = false;
	                while (true) {
			                Thread.sleep(500);
			                e.refresh();;
			                if (s!=e.isEngineOn()) {
					                    s = e.isEngineOn();
					                    System.out.println(s);
					                } 
			            }
	            } catch (Exception e) {
		                e.printStackTrace();
		            }
    
    
    
        }
}
