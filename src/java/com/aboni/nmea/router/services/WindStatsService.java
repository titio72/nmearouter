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

package com.aboni.nmea.router.services;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.metrics.WindStats;
import com.aboni.nmea.router.data.metrics.WindStatsReader;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class WindStatsService extends JSONWebService {

    @Inject
    public WindStatsService(@NotNull final WindStatsReader reader, @NotNull Log log) {
        super(log);
        setLoader((ServiceConfig config) -> {
            Instant from = config.getParamAsInstant("from", Instant.now().minusSeconds(86400L), 0);
            Instant to = config.getParamAsInstant("to", Instant.now(), 0);
            String sTicks = config.getParameter("ticks", "36");
            int ticks;
            try {
                ticks = Integer.parseInt(sTicks);
            } catch (Exception e) {
                ticks = 36;
            }
            try {
                WindStats stats = reader.getWindStats(from, to, ticks);
                JSONObject res = new JSONObject();
                List<JSONObject> l = new ArrayList<>(360);
                for (int i = 0; i < ticks; i++) {
                    JSONObject sample = new JSONObject();
                    sample.put("angle", i * (360 / ticks));
                    sample.put("windDistance", Utils.round(stats.getWindDistance(i), 1));
                    sample.put("windMaxSpeed", Utils.round(stats.getWindMaxSpeed(i), 1));
                    if (stats.getWindTime(i) != 0)
                        sample.put("windAvgSpeed", Utils.round(stats.getWindDistance(i) / (stats.getWindTime(i) / 3600.0), 1));
                    else
                        sample.put("windAvgSpeed", 0.0);
                    l.add(sample);
                }
                res.put("values", new JSONArray(l));
                res.put("interval", stats.getTotalTime());
                res.put("maxValue", Utils.round(stats.getMaxWindDistance(), 1));
                res.put("maxValueH", Utils.round(stats.getMaxWindDistance() / (stats.getTotalTime() / 3600.0), 1));
                res.put("tot", stats.getTot());
                return res;
            } catch (DataManagementException e) {
                log.errorForceStacktrace(LogStringBuilder.start("WindStatService").wO("execute").toString(), e);
                return null;
            }
        });
    }
}