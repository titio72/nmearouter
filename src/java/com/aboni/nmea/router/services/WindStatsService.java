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

import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.metrics.WindStats;
import com.aboni.nmea.router.data.metrics.WindStatsReader;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.data.Query;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;

public class WindStatsService extends JSONWebService {

    @Inject
    public WindStatsService(final QueryFactory queryFactory, final WindStatsReader reader, Log log) {
        super(log);
        setLoader((ServiceConfig config) -> {
            Query query = queryFactory.getQuery(config);
            String sTicks = config.getParameter("ticks", "36");
            int ticks;
            try {
                ticks = Integer.parseInt(sTicks);
            } catch (Exception e) {
                ticks = 36;
            }
            try {
                WindStats stats = reader.getWindStats(query, ticks);
                return stats.toJSON();
            } catch (DataManagementException e) {
                log.errorForceStacktrace(LogStringBuilder.start("WindStatService").wO("execute").toString(), e);
                return null;
            }
        });
    }
}