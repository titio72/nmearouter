package com.aboni.sensors;

import com.aboni.misc.DataFilter;
import com.aboni.sensors.hw.Atmospheric;
import com.aboni.sensors.hw.BME280;
import com.aboni.sensors.hw.BMP180;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import java.io.IOException;

public class SensorPressureTemp extends I2CSensor {

    public enum Sensor {
        BME280,
        BMP180
    }

    private double pressurePA;
    private double temperatureC;
    private double humidity;
    private Atmospheric atmospheric;
    private final Sensor sensor;

    public SensorPressureTemp(Sensor s) {
        super();
        sensor = s;
    }

    protected Atmospheric createAtmo(int bus) throws IOException, UnsupportedBusNumberException {
        if (sensor == Sensor.BMP180)
            return new BMP180(new I2CInterface(bus, BMP180.BMP180_ADDRESS));
        else
            return new BME280(new I2CInterface(bus, BME280.BME280_I2CADDR));
    }

    @Override
    protected void initSensor(int bus) throws SensorException {
        try {
            pressurePA = 0.0;
            temperatureC = 0.0;
            humidity = 0.0;
            setDefaultSmoothingAlpha(0.4);
            atmospheric = createAtmo(bus);
        } catch (IOException | UnsupportedBusNumberException e) {
            throw new SensorException("Error initializing MPU60050", e);
        }
    }

	public double getPressureMB() {
        return pressurePA * 0.01;
    }
	
	public double getHumidity() {
		return humidity;
	}
    
	@Override
	protected void readSensor() {
	    readPressurePA();
	    readTemperatureCelsius();
	    readHumidity();
	}

    private void readPressurePA() {
        double p = atmospheric.readPressure();
        pressurePA = DataFilter.getLPFReading(getDefaultSmoothingAlpha(), pressurePA, p);
    }

    private void readHumidity() {
        double h = atmospheric.readHumidity();
        humidity = DataFilter.getLPFReading(getDefaultSmoothingAlpha(), humidity, h);
    }

    public double getAltitude(double seaLevelPressure) {
        return (float) (44330.0 * (1.0 - Math.pow(pressurePA / seaLevelPressure, 0.1903)));
    }

    private void readTemperatureCelsius() {
        temperatureC = DataFilter.getLPFReading(getDefaultSmoothingAlpha(), temperatureC, atmospheric.readTemperature());
    }

    public double getTemperatureCelsius() {
        return temperatureC;
    }
		
    @Override
    public String getSensorName() {
        return "ATMO";
    }

}
