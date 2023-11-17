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

import com.aboni.nmea.router.utils.HWSettings;
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;

public abstract class I2CSensor implements Sensor {

    private String getMessage(String message) {
        return String.format("Sensor: name {%s} instance {%d} %s", getSensorName(), instance, message);
    }

    protected void log(String msg) {
        log.info(getMessage(msg));
    }

    protected void error(String msg, Throwable t) {
        if (t==null)
            log.error(getMessage(msg));
        else
            log.error(getMessage(msg), t);
    }

    private static final double LPF_ALPHA = 0.75;

    private static int instanceCounter;
    private final int instance;

    private double smoothingAlpha;

    private static final int MAX_FAILURES = 5;
    private final int maxFailures;
    private int failures;

    private boolean initialized;
    private final Log log;
    private long lastReadingTS;

    protected I2CSensor(Log log) {
        this.log = SafeLog.getSafeLog(log);
        setDefaultSmoothingAlpha(LPF_ALPHA);
        instanceCounter++;
        instance = instanceCounter;
        initialized = false;
        failures = 0;
        maxFailures = MAX_FAILURES;
    }

    @Override
    public final void init() throws SensorException {
        init(getBus());
    }

    public final void init(int bus) throws SensorException {
        log(LogStringBuilder.start("I2CSensor").wO("init").wV("bus", bus).toString());
        initSensor(bus);
        initialized = true;
    }

    protected abstract void initSensor(int bus) throws SensorException;

    protected final boolean isInitialized() {
        return initialized;
    }

    protected abstract void readSensor() throws SensorException;

    @Override
    public final void read() throws SensorException {
        if (isInitialized() && (failures<maxFailures)) {
            try {
                readSensor();
                lastReadingTS = System.currentTimeMillis();
            } catch (Exception e) {
                failures++;
                error(LogStringBuilder.start("I2CSensor").wO("read").wV("failure", failures).wV("max failures", maxFailures).toString(), e);
            }
        } else throw new SensorException("Sensor not initialized!");
    }

    public double getDefaultSmoothingAlpha() {
        return smoothingAlpha;
    }

    public void setDefaultSmoothingAlpha(double smoothingAlpha) {
        this.smoothingAlpha = smoothingAlpha;
    }

    protected int getBus() {
        return HWSettings.getPropertyAsInteger("bus", 1);
    }

    @Override
    public long getLastReadingTimestamp() {
        return lastReadingTS;
    }
}