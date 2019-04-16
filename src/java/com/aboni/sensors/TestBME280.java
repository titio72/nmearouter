package com.aboni.sensors;

@SuppressWarnings("InfiniteLoopStatement")
public class TestBME280 {

	public static void main(String[] args) {
		SensorPressureTemp sp = new SensorPressureTemp(SensorPressureTemp.Sensor.BME280);
		try {
			sp.init(1);
			while (true) {
				sp.read();
				System.out.format("P %.0fmb T %.2f°C H %.2f%%\r" , sp.getPressureMB(), sp.getTemperatureCelsius(), sp.getHumidity());
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
