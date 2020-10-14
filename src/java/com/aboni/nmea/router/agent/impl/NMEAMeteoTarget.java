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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class NMEAMeteoTarget extends NMEAAgentImpl {

    private final StatsWriter writer;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private final TimestampProvider timestampProvider;
    private final NMEACache cache;
    private int timerCount;

    private static final int TEMP = 0;
    private static final int W_TEMP = 1;
    private static final int PRESS = 2;
    private static final int WIND = 3;
    private static final int WIND_D = 4;
    private static final int HUM = 5;

    private final StatsSample[] series = new StatsSample[]{
            new ScalarStatsSample("AT0", -20.0, 50.0),
            new ScalarStatsSample("WT_", -20.0, 50.0),
            new ScalarStatsSample("PR_", 800.0, 1100.0),
            new ScalarStatsSample("TW_", 0.0, 100.0),
            new AngleStatsSample("TWD"),
            new ScalarStatsSample("HUM", 0.0, 150.0)
    };

    /* Cycles of timer*/
    private final int[] periods = new int[] {
            /*AT0*/ 10,
            /*WT_*/ 10,
            /*PR_*/  5,
            /*TW_*/  1,
            /*TWD*/  1,
            /*HUM*/ 10
    };

    private final int[] statsPeriodCounter = new int[]{
            /*AT0*/ 0,
            /*WT_*/ 0,
            /*PR_*/ 0,
            /*TW_*/ 0,
            /*TWD*/ 0,
            /*HUM*/ 0
    };

    private final Log log;

    @Inject
    public NMEAMeteoTarget(@NotNull Log log, @NotNull NMEACache cache, @NotNull TimestampProvider tp, @NotNull @Named(Constants.TAG_METEO) StatsWriter w) {
        super(log, tp, false, true);
        this.timestampProvider = tp;
        this.cache = cache;
        this.log = log;
        this.writer = w;
    }

    @Override
    public final String getDescription() {
        return "Meteo data sampling";
    }

    @Override
    public String getType() {
        return "Meteo data tracker";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected boolean onActivate() {
        try {
            if (writer != null) writer.init();
            return true;
        } catch (Exception e) {
            getLogBuilder().wO("activate").errorForceStacktrace(log, e);
            return false;
        }
    }


    @Override
    public void onTimer() {
        timerCount = (timerCount+1) % SAMPLING_FACTOR;
        if (timerCount==0) dumpStats();
        super.onTimer();
    }


    private void dumpStats() {
        synchronized (series) {
            long ts = timestampProvider.getNow();
            for (int i = 0; i < series.length; i++) {
                statsPeriodCounter[i]++;
                if (statsPeriodCounter[i] >= periods[i]) {
                    StatsSample series1 = series[i];
                    write(series1, ts);
                    series1.reset();
                    statsPeriodCounter[i] = 0;
                }
            }
        }
    }

    @Override
    protected void onDeactivate() {
        if (isStarted() && writer != null) {
            writer.dispose();
        }
    }

    @OnRouterMessage
    public void onSentence(RouterMessage msg) {
        Message m = msg.getMessage();
        try {
            if (Boolean.TRUE.equals(cache.getStatus(NMEARouterStatuses.GPS_TIME_SYNC, false))) {
                if (m instanceof MsgGenericTemperature &&
                        "Main Cabin Temperature".equals(((MsgGenericTemperature) m).getTemperatureSource())) {
                    processTemp(((MsgGenericTemperature) m).getTemperature());
                }

                if (m instanceof MsgGenericAtmosphericPressure) {
                    processPressure(((MsgGenericAtmosphericPressure) m).getAtmosphericPressure());
                }

                if (m instanceof MsgGenericTemperature &&
                        "Sea Temperature".equals(((MsgGenericTemperature) m).getTemperatureSource())) {
                    processWaterTemp(((MsgGenericTemperature) m).getTemperature());
                }

                if (m instanceof MsgGenericHumidity) {
                    processHumidity(((MsgGenericHumidity) m).getHumidity());
                }

                if (m instanceof MsgWindData) {
                    processWind((MsgWindData) m);
                }
            }
        } catch (Exception e) {
            getLogBuilder().wO("process sentence").wV("sentence", m).error(log, e);
        }
    }

    private void collect(int id, double d) {
        synchronized (series) {
            StatsSample s = series[id];
            s.add(d);
        }
    }

    private void write(StatsSample s, long ts) {
        if (writer!=null && s!=null && s.getSamples()>0) {
            if ("TW_".equals(s.getTag()) && s.getAvg() < 10.0 && s.getMax() > (s.getAvg() * 4.5)) {
                // skip anomalous reading (like 80kn of max with avg of 4kn)
                return;
            }
            writer.write(s, ts);
        }
    }

    private void processWind(MsgWindData s) {
        if (s.isTrue()) {
            DataEvent<MsgHeading> e = cache.getLastHeading();
            if (!cache.isHeadingOlderThan(timestampProvider.getNow(), 800)) {
                double windDir = e.getData().getHeading() + s.getAngle();
                double windSpd = s.getSpeed();
                collect(WIND, windSpd);
                collect(WIND_D, windDir);
            }
        }
    }

    private void processWaterTemp(double temperature) {
        if (!Double.isNaN(temperature)) collect(W_TEMP, temperature);
    }

    private void processPressure(double pressure) {
        if (!Double.isNaN(pressure)) collect(PRESS, pressure);
    }

    private void processTemp(double temperature) {
        if (!Double.isNaN(temperature)) collect(TEMP, temperature);
    }

    private void processHumidity(double humidity) {
        if (!Double.isNaN(humidity)) collect(HUM, humidity);
    }
}
