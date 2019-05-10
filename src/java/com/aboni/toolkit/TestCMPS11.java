package com.aboni.toolkit;

import com.aboni.sensors.SensorCMPS11;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestCMPS11 {

	public static void main(String[] args) {
		SensorCMPS11 sp = new SensorCMPS11();
		try {
			sp.init(1);
			while (true) {
				sp.read();
				System.out.format("H %.0f %d d\r" , sp.getHeading(), sp.getHeading255());
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Error", e);
		}
	}
}
