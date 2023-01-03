/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.sensors;

import com.aboni.nmea.router.utils.Log;
import com.aboni.sensors.hw.Atmospheric;
import com.aboni.sensors.hw.BME280;
import com.aboni.sensors.hw.BMP180;
import com.aboni.utils.DataFilter;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

    public SensorPressureTemp(Sensor s, @NotNull Log log) {
        super(log);
        sensor = s;
    }

    @Inject
    public SensorPressureTemp(@NotNull Log log) {
        this(Sensor.BME280, log);
    }

    protected Atmospheric createAtmo(int bus) throws IOException, UnsupportedBusNumberException {
        if (sensor == Sensor.BMP180)
            return new BMP180(new I2CInterface(bus, BMP180.BMP180_ADDRESS));
        else
            return new BME280(new I2CInterface(bus, BME280.BME280_I2C_ADDRESS));
    }

    @Override
    protected void initSensor(int bus) throws SensorException {
        try {
            pressurePA = Double.NaN;
            temperatureC = Double.NaN;
            humidity = Double.NaN;
            setDefaultSmoothingAlpha(0.4);
            atmospheric = createAtmo(bus);
            readSensor();
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
