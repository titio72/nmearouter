package com.aboni.sensors;

import java.io.IOException;

import com.aboni.sensors.hw.Atmo;
import com.aboni.sensors.hw.BME280;
import com.aboni.sensors.hw.BMP180;
import com.aboni.utils.DataFilter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

public class SensorPressureTemp extends I2CSensor {
	
	public enum Sensor {
		BME280,
		BMP180
	}
	
	private double pressurePA;
	private double temperatureC;
	private double humidity;
	private Atmo atmo;
	private Sensor sensor;
	
	public SensorPressureTemp() {
		super();
		sensor = Sensor.BMP180;
	}
	
	public SensorPressureTemp(Sensor s) {
		super();
		sensor = s;
	}
	
	protected Atmo createAtmo(int bus) throws IOException, UnsupportedBusNumberException {
		if (sensor==Sensor.BMP180)
			return new BMP180(new I2CInterface(bus, BMP180.BMP180_ADDRESS));
		else 
			return new BME280(new I2CInterface(bus, BME280.BME280_I2CADDR));
	}	
	
	@Override
	protected void _init(int bus) throws IOException, UnsupportedBusNumberException {
		pressurePA = 0.0;
		temperatureC = 0.0;
		humidity = 0.0;
		setDefaultSmootingAlpha(0.4);
		atmo = createAtmo(bus);
	}

	public double getPressureMB() {
        return pressurePA * 0.01;
    }
	
	public double getPressurePA() {
        return pressurePA;
    }
	
	public double getHumidity() {
		return humidity;
	}
    
	@Override
	protected void _read() throws Exception {
	    _readPressurePA();
	    _readTemperatureCelsius();
	    _readHumidity();
	}
	
	private void _readPressurePA() throws Exception {
    	double p = atmo.readPressure();
		pressurePA = DataFilter.getLPFReading(getDefaultSmootingAlpha(), pressurePA, p);
	}
	
	private void _readHumidity() throws Exception {
    	double h = atmo.readHumidity();
		humidity = DataFilter.getLPFReading(getDefaultSmootingAlpha(), humidity, h);
	}

	public double getAltitude(double sealevelPressure) {
		float altitude = (float) (44330.0 * (1.0 - Math.pow(pressurePA / sealevelPressure, 0.1903)));
		return altitude;
	}

	private void _readTemperatureCelsius() throws Exception {
        temperatureC = DataFilter.getLPFReading(getDefaultSmootingAlpha(), temperatureC, atmo.readTemperature());
    }

	public double getTemperatureCelsius() {
	    return temperatureC;
	}
		
    @Override
    public String getSensorName() {
        return "ATMO";
    }

}
