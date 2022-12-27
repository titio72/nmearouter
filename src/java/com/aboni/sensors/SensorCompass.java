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

import com.aboni.geo.DeviationManager;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.DataFilter;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;

public class SensorCompass extends I2CSensor {

    public static final String COMPASS_SENSOR_CATEGORY = "CompassSensor";

    public static class Data {
        private double pitch;
        private double roll;
        private double head;
    }

    private double compassSmoothing = 0.75;
    private double attitudeSmoothing = 0.75;
    private long lastModifiedDevTable;
    private final Data data = new Data();
    private final Data unfilteredData = new Data();
    private final CompassDataProvider provider;
    private final DeviationManager devManager;

    @Inject
    public SensorCompass(@NotNull Log log, @NotNull CompassDataProvider provider, DeviationManager deviationManager) {
        super(log);
        this.provider = provider;
        this.devManager = deviationManager;
    }

    @Override
    protected final void initSensor(int bus) throws SensorException {
        provider.init();
    }

    /**
     * Get pitch in degrees without any smoothing.
     *
     * @return The pitch value in degrees
     */
    public double getUnfilteredPitch() {
        return unfilteredData.pitch;
    }

    /**
     * Get the roll in degrees without smoothing.
     *
     * @return The value in degrees
     */
    public double getUnfilteredRoll() {
        return unfilteredData.roll;
    }

    /**
     * Get the heading in degrees [0..360].
     *
     * @return The heading in degrees
     */
    public double getUnfilteredSensorHeading() {
        return unfilteredData.head;
    }

    /**
     * Filtered reading of the sensor (smoothed roll).
     *
     * @return the roll
     */
    public double getPitch() {
        return data.pitch;
    }

    /**
     * Filtered reading of the sensor (smoothed roll).
     *
     * @return the roll
     */
    public double getRoll() {
        return data.roll;
    }

    /**
     * Filtered reading of the sensor
     *
     * @return the heading
     */
    public double getSensorHeading() {
        return data.head;
    }

    /**
     * Get the magnetic heading in degrees compensated with the deviation table[0..360] and smoothed
     *
     * @return the heading in degrees
     */
    public double getHeading() {
        return devManager.getMagnetic(getSensorHeading());
    }

    public void loadConfiguration() {
        updateDeviationTable();
        attitudeSmoothing = HWSettings.getPropertyAsDouble("attitude.smoothing", 0.75);
        compassSmoothing = HWSettings.getPropertyAsDouble("compass.smoothing", 0.75);
        provider.refreshConfiguration();
    }

    private void updateDeviationTable() {
        try {
            File f = new File(Constants.DEVIATION);
            if (f.exists() && f.lastModified() > lastModifiedDevTable) {
                log(LogStringBuilder.start(COMPASS_SENSOR_CATEGORY).wO("load deviation table").toString());
                lastModifiedDevTable = f.lastModified();
                try (FileInputStream s = new FileInputStream(f)) {
                    devManager.reset();
                    if (!devManager.load(s)) {
                        log(LogStringBuilder.start(COMPASS_SENSOR_CATEGORY).wO("fail load deviation table").toString());
                    }
                }
            }
        } catch (Exception e) {
            error(LogStringBuilder.start(COMPASS_SENSOR_CATEGORY).wO("load deviation table").toString(), e);
        }
    }

    @Override
    protected void readSensor() throws SensorException {
        double[] res = provider.read();
        if (res != null) {
            unfilteredData.pitch = res[0];
            unfilteredData.roll = res[1];
            unfilteredData.head = res[2];
            data.pitch = DataFilter.getLPFReading(attitudeSmoothing, data.pitch, res[0]);
            data.roll = DataFilter.getLPFReading(attitudeSmoothing, data.roll, res[1]);
            data.head = DataFilter.getLPFReading(compassSmoothing, data.head, res[2]);
        }
    }

    @Override
    public String getSensorName() {
        return "COMPASS";
    }
}
