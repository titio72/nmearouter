package com.aboni.sensors.hw;

import java.io.FileInputStream;
import java.io.IOException;

public class CPUTemp {

	private byte[] bf = new byte[6];
	
	public double read() throws IOException {
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
	
}
