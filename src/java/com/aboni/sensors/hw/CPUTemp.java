package com.aboni.sensors.hw;

import java.io.FileInputStream;

import com.aboni.misc.Sample;
import com.aboni.utils.ServerLog;

public class CPUTemp {

	private static final int READ_THRESHOLD = 1999;

	private final byte[] bf = new byte[6];
	
	private Sample temp = new Sample(0, 0);

	private final boolean arm;

	private static final CPUTemp instance = new CPUTemp();
	
	private CPUTemp() {
		String name = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
    	arm = (arch.startsWith("arm") || name.contains("inux"));
	}
	
	public static CPUTemp getInstance() {
		return instance;
	}
	
	private double read() {
		try {
			FileInputStream f = new FileInputStream("/sys/class/thermal/thermal_zone0/temp");
			int rr = f.read(bf);
			if (rr > 0) {
				String s = new String(bf, 0, rr);
				return Double.parseDouble(s) / 1000.0;
			}
			f.close();
		} catch (Exception e) {
			ServerLog.getLogger().Debug("Cannot read cpu temperature {" + e.getMessage() + "}");
		}
		return 0;
	}
	
	public double getTemp() {
		synchronized (this) {
			if (arm) {
				long t = System.currentTimeMillis();
				if (temp.getAge(t) > READ_THRESHOLD) {
					temp = new Sample(t, read());
				}
			}
		}
		return temp.getValue();
	}
}
