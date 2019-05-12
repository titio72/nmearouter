package com.aboni.toolkit;

import com.aboni.sensors.SensorVoltage;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestVoltage {

	private static final String FORMAT = "%.3f";

	public static void main(String[] args) {

		new Tester(500).start(new Tester.TestingProc() {
			final SensorVoltage v = new SensorVoltage(0x48);

			@Override
			public boolean doIt(PrintStream out) {
				try {
					v.read();
					out.format(FORMAT, v.getVoltage0());
					out.format(FORMAT, v.getVoltage1());
					out.format(FORMAT, v.getVoltage2());
					out.format(FORMAT, v.getVoltage3());
					out.println();
					return true;
				} catch (Exception e) {
					e.printStackTrace(out);
					return false;
				}
			}

			@Override
			public boolean init(PrintStream out) {
				return true;
			}

			@Override
			public void shutdown(PrintStream out) {
				// nothing to bring down
			}
		});
	}
}
