package com.aboni.sensors;

public class TestCMPS11 {

	public static void main(String[] args) {
		SensorCMPS11 sp = new SensorCMPS11();
		try {
			sp.init(1);
			while (true) {
				sp.read();
				System.out.format("H %.0f d\r" , sp.getHeading(), sp.getHeading255());
				Thread.sleep(1000);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
