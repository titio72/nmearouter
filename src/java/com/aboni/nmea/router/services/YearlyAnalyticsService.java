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

import com.aboni.nmea.router.data.track.MonthYearTrackStats;
import com.aboni.log.Log;
import org.json.JSONObject;

import javax.inject.Inject;

public class YearlyAnalyticsService extends JSONWebService {

    private final MonthYearTrackStats monthYearTrackStats;

    @Inject
    public YearlyAnalyticsService(final MonthYearTrackStats manager, Log log) {
        super(log);
        if (manager==null) throw new IllegalArgumentException("Track manager is null");
        this.monthYearTrackStats = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            return monthYearTrackStats.getYearlyStats();
        } catch (Exception e) {
            throw new JSONGenerationException("Error reading yearly stats", e);
        }
    }
}