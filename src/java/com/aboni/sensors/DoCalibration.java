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
	    
	    /*
		try {
			double[] d = new double[6];
			m.init(1);
			System.out.println("Start");
			long t0 = System.currentTimeMillis();
			while ((System.currentTimeMillis()-t0)<(SECONDS*1000)) {
				m.read();
				double[] reading = m.getMagVector();
				for (int i = 0; i<3; i++) {
					d[2*i] = Math.min(d[2*i], reading[i]);
					d[2*i+1] = Math.max(d[2*i+1], reading[i]);
				}
				Thread.sleep(100);
			}  
			for (int i = 0; i<3; i++) {
				System.out.println((d[2*i]+d[2*i+1])/2);
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
	}
}
