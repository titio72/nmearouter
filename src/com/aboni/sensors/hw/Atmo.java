package com.aboni.sensors.hw;

public interface Atmo {

	float readTemperature();

	float readPressure();

	float readHumidity();

	void setStandardSeaLevelPressure(int standardSeaLevelPressure);

	double readAltitude();

}