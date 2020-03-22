package com.aboni.sensors.hw;

public interface Atmospheric {

    float readTemperature();

    float readPressure();

    float readHumidity();

    void setStandardSeaLevelPressure(int standardSeaLevelPressure);

    double readAltitude();

}