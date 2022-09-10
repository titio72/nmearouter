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

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.metrics.WindStats;
import com.aboni.nmea.router.data.metrics.WindStatsReader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DBWindStatsReader implements WindStatsReader {

    private final DataReader reader;

    private static class StatsContext implements DataReader.DataReaderListener {

        StatsContext(int sectors) {
            stats = new WindStats(sectors);
        }

        double wSpeed;
        double wAngle;
        long wSpeedTime;
        long wAngleTime;
        final WindStats stats;

        @Override
        public void onRead(Sample sample) {
            if ("TW_".equals(sample.getTag())) {
                if ((sample.getTs() - wAngleTime) < 250) {
                    stats.addSample(60, wAngle, sample.getValue());
                } else {
                    wSpeed = sample.getValue();
                    wSpeedTime = sample.getTs();
                }
            } else if ("TWD".equals(sample.getTag())) {
                if ((sample.getTs() - wSpeedTime) < 250) {
                    stats.addSample(60, sample.getValue(), wSpeed);
                } else {
                    wAngle = sample.getValue();
                    wAngleTime = sample.getTs();
                }
            }
        }
    }

    @Inject
    public DBWindStatsReader(@NotNull @Named(Constants.TAG_METEO) DataReader reader) {
        this.reader = reader;
    }

    @Override
    public WindStats getWindStats(Instant from, Instant to, int sectors) throws DataManagementException {

        if (360 % sectors != 0) throw new DataManagementException("Number of sectors must divide 360");
        StatsContext ctx = new StatsContext(sectors);
        reader.readData(from, to, ctx);
        return ctx.stats;
    }
}
