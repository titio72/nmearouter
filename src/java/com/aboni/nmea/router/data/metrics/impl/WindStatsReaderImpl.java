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

package com.aboni.nmea.router.data.metrics.impl;

import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.data.metrics.WindStats;
import com.aboni.nmea.router.data.metrics.WindStatsReader;

import javax.inject.Inject;
import javax.inject.Named;

public class WindStatsReaderImpl implements WindStatsReader {

    private final DataReader reader;
    private final int defaultIntervalSeconds;

    private static class StatsContext implements DataReader.DataReaderListener {

        private final int defaultIntervalSeconds;
        private final WindStats stats;
        private Sample lastAngle = null;
        private Sample lastSpeed = null;
        private long lastSampleTime = 0;

        StatsContext(int sectors, int defaultIntervalSeconds) {
            stats = new WindStats(sectors);
            this.defaultIntervalSeconds = defaultIntervalSeconds;
        }

        @Override
        public void onRead(Sample sample) {
            if (Metrics.WIND_SPEED.getId().equals(sample.getTag())) {
                if (lastAngle!=null && (sample.getTimestamp() - lastAngle.getTimestamp()) < 250) {
                    feedWindStats(sample.getTimestamp(), sample, lastAngle);
                } else {
                    lastSpeed = sample;
                }
            } else if (Metrics.WIND_DIRECTION.getId().equals(sample.getTag())) {
                if (lastSpeed!=null && (sample.getTimestamp() - lastSpeed.getTimestamp()) < 250) {
                    feedWindStats(sample.getTimestamp(), lastSpeed, sample);
                } else {
                    lastAngle = sample;
                }
            }
        }

        private void feedWindStats(long time, Sample speed, Sample angle) {
            if (lastSampleTime!=0) {
                // skip the first sample as we do not know the elapsed time
                stats.addSample(
                        (int) ((time - lastSampleTime) / 1000L),
                        angle.getValue(), speed.getValue());
            } else {
                stats.addSample(
                        defaultIntervalSeconds,
                        angle.getValue(), speed.getValue());
            }
            lastAngle = null;
            lastSpeed = null;
            lastSampleTime = time;
        }
    }

    @Inject
    public WindStatsReaderImpl(@Named(Constants.TAG_METEO) DataReader reader) {
        this(reader, 60);
    }

    WindStatsReaderImpl(DataReader reader, int defaultIntervalSeconds) {
        if (reader==null) throw new IllegalArgumentException("Reader is null");
        this.reader = reader;
        this.defaultIntervalSeconds = defaultIntervalSeconds;
    }

    @Override
    public WindStats getWindStats(Query query, int sectors) throws DataManagementException {
        if (360 % sectors != 0) throw new DataManagementException("Number of sectors must divide 360");
        StatsContext ctx = new StatsContext(sectors, defaultIntervalSeconds);
        reader.readData(query, ctx);
        return ctx.stats;
    }
}
