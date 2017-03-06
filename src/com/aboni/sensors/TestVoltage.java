package com.aboni.sensors;

public class TestVoltage {
	public static void main(String[] args) {
		SensorVoltage v = new SensorVoltage(0x48);
		try {
			v.init(1);
			while (true) {
				v.read();
		        System.out.format("%.3f%n",v.getVoltage0());
		        System.out.format("%.3f%n",v.getVoltage1());
		        System.out.format("%.3f%n",v.getVoltage2());
		        System.out.format("%.3f%n",v.getVoltage3());
		        Thread.sleep(500);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
