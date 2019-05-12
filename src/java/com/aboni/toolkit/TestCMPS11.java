package com.aboni.toolkit;

import com.aboni.sensors.SensorCMPS11;
import com.aboni.utils.Tester;

import java.io.PrintStream;

public class TestCMPS11 {

	public static void main(String[] args) {


		new Tester(1000).start(new Tester.TestingProc() {

			final SensorCMPS11 sp = new SensorCMPS11();


			@Override
			public boolean doIt(PrintStream out) {
				try {
					sp.read();
					out.format("H %.0f %d d\r" , sp.getHeading(), sp.getHeading255());
					return true;
				} catch (Exception e) {
					e.printStackTrace(out);
					return false;
				}
			}

			@Override
			public boolean init(PrintStream out) {
				try {
					sp.init(1);
					return true;
				} catch (Exception e) {
					e.printStackTrace(out);
					return false;
				}
			}

			@Override
			public void shutdown(PrintStream out) {
				// nothing to bring down
			}
		});
	}
}
