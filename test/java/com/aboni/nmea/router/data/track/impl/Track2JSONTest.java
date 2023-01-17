/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackManagementException;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackReader;
import com.aboni.nmea.router.data.Query;
import com.aboni.nmea.router.data.QueryById;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class Track2JSONTest {

    public Track2JSONTest() {
    }

    private class MyTrackReader implements TrackReader {

        private TrackPoint getSample(long ts, double lat, double lon) {
            TrackPoint s = new TrackPointBuilderImpl()
                    .withPosition(new GeoPositionT(ts, new Position(lat, lon)))
                    .withPeriod(60).getPoint();
            return s;
        }

        @Override
        public void readTrack(@NotNull Query q, @NotNull TrackReaderListener target) throws TrackManagementException {
            target.onRead(1001, getSample(10001000, 43.10000000, 10.08000000));
            target.onRead(1002, getSample(10061000, 43.10000100, 10.08000100));
            target.onRead(1003, getSample(10121000, 43.10000200, 10.08000200));
            target.onRead(1004, getSample(10181000, 43.10000300, 10.08000300));
        }
    }


    @Test
    public void testPath() throws Exception {
        Track2JSON j = new Track2JSON(new MyTrackReader());
        StringWriter w = new StringWriter();
        j.dump(new QueryById(1), w);
        JSONObject jTrack = new JSONObject(w.toString());
        JSONArray path = jTrack.getJSONObject("track").getJSONArray("path");

        assertEquals(4, path.length());

        assertEquals(43.10000000, ((JSONObject) path.get(0)).getDouble("lat"), 0.0000001);
        assertEquals(10.08000000, ((JSONObject) path.get(0)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000100, ((JSONObject)path.get(1)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000100, ((JSONObject)path.get(1)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000200, ((JSONObject)path.get(2)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000200, ((JSONObject)path.get(2)).getDouble("lng"), 0.0000001);
		
		assertEquals(43.10000300, ((JSONObject)path.get(3)).getDouble("lat"), 0.0000001);
		assertEquals(10.08000300, ((JSONObject)path.get(3)).getDouble("lng"), 0.0000001);
		
	}

}
