package com.aboni.toolkit;

import com.aboni.sensors.SensorException;
import com.aboni.sensors.SensorPressureTemp;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestBME280 {

	public static void main(String[] args) {
		new Tester(500).start(new Tester.TestingProc() {

			final SensorPressureTemp sp = new SensorPressureTemp(SensorPressureTemp.Sensor.BME280);

			@Override
			public boolean doIt(PrintStream out) {
				try {
					sp.read();
					out.format("P %.2fmb T %.2f°C H %.2f%%\r", sp.getPressureMB(), sp.getTemperatureCelsius(), sp.getHumidity());
					return true;
				} catch (Exception e) {
					e.printStackTrace(out);
					return false;
				}
			}

			@Override
			public boolean init(PrintStream out) {
				try {
					sp.init();
				} catch (SensorException e) {
					e.printStackTrace();
				}
				return true;
			}
		});
	}
}
