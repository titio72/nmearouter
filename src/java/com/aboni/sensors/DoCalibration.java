package com.aboni.sensors;

public class DoCalibration {

	private static final int SECONDS = 15;
	
	public static void main(String[] args) {
        SensorHMC5883 m = new SensorHMC5883();
        m.setDefaultSmootingAlpha(1.0);
        try {
            m.init(1);
            HMC5883Calibration cc = new HMC5883Calibration(m, SECONDS * 1000);
            System.out.println("Start");
            cc.start();
            System.out.println("Radius: " + cc.getRadius());
            System.out.println("StdDev: " + cc.getsDev());
            System.out.println("StdDev: " + cc.getsDev());
            System.out.println("C_X:    " + cc.getCalibration()[0]);
            System.out.println("C_Y:    " + cc.getCalibration()[1]);
            System.out.println("C_Z:    " + cc.getCalibration()[2]);
        } catch (Exception e1) {
            e1.printStackTrace();
        }

	}
}
