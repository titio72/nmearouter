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

package com.aboni.nmea.router.data.track;

import com.aboni.nmea.router.data.Query;
import org.json.JSONObject;

import javax.inject.Inject;

public class JSONTrackAnalytics {

    private final TrackReader reader;

    @Inject
    public JSONTrackAnalytics(TrackReader reader) {
        if (reader==null) throw new IllegalArgumentException("Track reader is null");
        this.reader = reader;
    }

    public JSONObject getAnalysis(Query query) throws TrackManagementException {
        if (query==null) throw new IllegalArgumentException("Query is null");
        TrackAnalytics analytics = new TrackAnalytics("");
        reader.readTrack(query, analytics::processSample);
        return analytics.getJSONStats();
    }

}
