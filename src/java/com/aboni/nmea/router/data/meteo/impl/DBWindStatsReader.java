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

package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.meteo.MeteoManagementException;
import com.aboni.nmea.router.data.meteo.MeteoReader;
import com.aboni.nmea.router.data.meteo.WindStats;
import com.aboni.nmea.router.data.meteo.WindStatsReader;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DBWindStatsReader implements WindStatsReader {

    private final MeteoReader reader;

    private class StatsContext implements MeteoReader.MeteoReaderListener {

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
    public DBWindStatsReader(@NotNull MeteoReader reader) {
        this.reader = reader;
    }

    @Override
    public WindStats getWindStats(Instant from, Instant to, int sectors) throws MeteoManagementException {

        if (360 % sectors != 0) throw new MeteoManagementException("Number of sectors must divide 360");
        StatsContext ctx = new StatsContext(sectors);
        reader.readMeteo(from, to, ctx);
        return ctx.stats;
    }
}
