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

import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.track.JSONTrackAnalytics;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.log.Log;

import javax.inject.Inject;

public class TrackAnalyticsService extends JSONWebService {

    @Inject
    public TrackAnalyticsService(Log log, QueryFactory queryFactory, JSONTrackAnalytics analytics) {
        super(log);
        if (queryFactory==null) throw new IllegalArgumentException("Query factory is null");
        if (analytics==null) throw new IllegalArgumentException("Track analytics is null");
        setLoader((ServiceConfig config) -> {
            try {
                Query q = queryFactory.getQuery(config);
                return analytics.getAnalysis(q);
            } catch (TrackManagementException e) {
                throw new JSONGenerationException("Error generating track stats json", e);
            }
        });
    }
}
