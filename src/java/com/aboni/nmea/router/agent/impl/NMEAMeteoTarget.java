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

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.utils.*;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class NMEAMeteoTarget extends NMEAAgentImpl {

    private final StatsWriter writer;

    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private static final int TEMP = 0;
    private static final int W_TEMP = 1;
    private static final int PRESS = 2;
    private static final int WIND = 3;
    private static final int WIND_D = 4;
    private static final int HUM = 5;

    private final StatsSample[] series = new StatsSample[] {
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

    private boolean useMWD;

    @Inject
    public NMEAMeteoTarget(@NotNull NMEACache cache, @NotNull @Named(Constants.TAG_METEO) StatsWriter w) {
        super(cache);
        setSourceTarget(false, true);
        writer = w;
    }

    @Override
    protected final void onSetup(String name, QOS qos) {
        useMWD = qos != null && qos.get("useMWD");
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
            getLogger().error("Error connecting db Agent {Meteo}", e);
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
            long ts = getCache().getNow();
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

    @OnSentence
    public void onSentence(Sentence s, String source) {
        try {
            if (Boolean.TRUE.equals(getCache().getStatus(NMEARouterStatuses.GPS_TIME_SYNC, false))) {
                if (s instanceof MTASentence) {
                    processTemp((MTASentence) s);
                } else if (s instanceof MMBSentence) {
                    processPressure((MMBSentence) s);
                } else if (s instanceof MTWSentence) {
                    processWaterTemp((MTWSentence) s);
                } else if (s instanceof MHUSentence) {
                    processHumidity((MHUSentence) s);
                } else if (useMWD && s instanceof MWDSentence) {
                    processWind((MWDSentence) s);
                } else if (!useMWD && s instanceof MWVSentence) {
                    processWind((MWVSentence)s);
                }
            }
        } catch (Exception e) {
            getLogger().warning("Error processing meteo stats {" + s + "} error {" + e + "}");
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

    private void processWind(MWDSentence s) {
        if (Double.isNaN(s.getWindSpeedKnots())) {
            collect(WIND, s.getWindSpeed() * 1.94384);
        } else {
            collect(WIND, s.getWindSpeedKnots());
        }
        collect(WIND_D, s.getMagneticWindDirection());
    }

    private void processWind(MWVSentence s) {
        if (s.isTrue()) {
            DataEvent<HeadingSentence> e = getCache().getLastHeading();
            if (e != null && (getCache().getNow() - e.getTimestamp()) < 800) {
                double windDir = e.getData().getHeading() + s.getAngle();
                double windSpd;
                switch (s.getSpeedUnit().toChar()) {
                    case 'N':
                        windSpd = s.getSpeed();
                        break;
                    case 'K':
                        windSpd = s.getSpeed() / 1.852;
                        break;
                    case 'M':
                        windSpd = s.getSpeed() * 1.94384;
                        break;
                    default:
                        windSpd = 0.0;
                }
                collect(WIND, windSpd);
                collect(WIND_D, windDir);
            }
        }
    }

    private void processWaterTemp(MTWSentence s) {
        collect(W_TEMP, s.getTemperature());
    }

    private void processPressure(MMBSentence s) {
        collect(PRESS, s.getBars() * 1000);
    }

    private void processTemp(MTASentence s) {
        collect(TEMP, s.getTemperature());
    }

    private void processHumidity(MHUSentence s) {
        collect(HUM, s.getRelativeHumidity());
    }
}
