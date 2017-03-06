package com.aboni.sensors.hw;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class TestLed {

	private static GpioPinDigitalOutput pins[] = new GpioPinDigitalOutput[30];
	
	private static GpioPinDigitalOutput getPin(int i) {
		if (pins[i]!=null) return pins[i];
		else {
	        GpioController gpio = GpioFactory.getInstance();
			pins[i] = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + i), "p" + i, PinState.LOW);
			return pins[i];
		}
	}
	
	public static void main(String[] args) {
		for (Pin rp: RaspiPin.allPins()) {
			Pin pp = RaspiPin.getPinByName(rp.getName());
			System.out.println("p " + rp.getName() + " " + (rp==pp));
		}
		

        while (true) {
	        try {
	        	byte[] b =  new byte[256];
	        	System.in.read(b);
	        	String input = new String(b);
	        	String _p = input.split(" ")[0].trim();
	        	String _s = input.split(" ")[1].trim();
	        	
	        	System.out.println("Requested Pin " + _p + " [" + _s + "]");

	            GpioPinDigitalOutput pin = getPin(Integer.parseInt(_p));
	            pin.setShutdownOptions(true, PinState.LOW);
	            if ("1".equals(_s)) {
	                pin.high();
	            } else if ("0".equals(_s)) {
	                pin.low();
	            } 
	            System.out.println("Pin " + pin.getName() + " [" + pin.getState() + "]");
	
	            
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
        
	}
	
}
