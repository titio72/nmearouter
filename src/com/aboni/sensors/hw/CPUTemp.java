package com.aboni.sensors.hw;

import java.io.FileInputStream;
import java.io.IOException;

import com.aboni.misc.Sample;
import com.aboni.utils.ServerLog;

public class CPUTemp {

	private static final int READ_THRESHOLD = 1999;

	private static byte[] bf = new byte[6];
	
	private static Sample temp = new Sample(0, 0);
	
	private static double read() throws IOException {
		FileInputStream f = new FileInputStream("/sys/class/thermal/thermal_zone0/temp");
		try {
			int rr = f.read(bf);
			if (rr>0) {
				String s = new String(bf, 0, rr);
				return Double.parseDouble(s) / 1000.0;
			}
		} catch (Exception e) {
		} finally {
			f.close();
		}
		return 0;
	}
	
	public static double getTemp() {
		synchronized (temp) {
			try {
				long t = System.currentTimeMillis();
				if (temp.getAge(t)>READ_THRESHOLD) {
					temp = new Sample(t, read());
				}
				return temp.getValue();
			} catch (Exception e) {
				ServerLog.getLogger().Error("Error reading cpu temperature", e);
			}
			return 0.0;
		}
	}
}
