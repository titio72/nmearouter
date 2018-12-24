package com.aboni.sensors.hw;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Fan {
    private GpioPinDigitalOutput pin;
    private final boolean arm;
	private boolean fanOn;
    
	public boolean isRaspberry() {
		return arm;
	}
	
    public Fan() {
    	arm = (System.getProperty("os.arch").startsWith("arm"));
    	if (arm) {
	        GpioController gpio = GpioFactory.getInstance();
	        pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "fan", PinState.LOW);
	        pin.setShutdownOptions(true, PinState.LOW);
    	}
    }
    
	public void switchFan(boolean on) {
		if (arm) {
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
