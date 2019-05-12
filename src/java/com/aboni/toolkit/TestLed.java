package com.aboni.toolkit;

import com.aboni.utils.Tester;
import com.pi4j.io.gpio.*;

import java.io.PrintStream;

public class TestLed implements Tester.TestingProc {

    private static final GpioPinDigitalOutput[] pins = new GpioPinDigitalOutput[30];
	
	private static GpioPinDigitalOutput getPin(int i) {
		if (pins[i]!=null) return pins[i];
		else {
	        GpioController gpio = GpioFactory.getInstance();
			pins[i] = gpio.provisionDigitalOutputPin(RaspiPin.getPinByName("GPIO " + i), "p" + i, PinState.LOW);
			return pins[i];
		}
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

    @Override
    public void shutdown(PrintStream out) {
		// nothing to shutdown
	}

    public static void main(String[] args) {
	    TestLed l = new TestLed();
		new Tester(0).start(l);
	}
	
}
