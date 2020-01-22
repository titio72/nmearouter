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
