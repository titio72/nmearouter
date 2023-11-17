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

import com.aboni.geo.GeoPositionT;
import net.sf.marineapi.nmea.util.Position;

import java.util.LinkedList;
import java.util.List;

public class AveragePosition {

    private final List<GeoPositionT> positions;

    private int samples;
    private double avgLat;
    private double avgLon;

    private static final long PERIOD = 5L * 60000L; //5 minutes

    public AveragePosition() {
        positions = new LinkedList<>();
    }

    public void addPosition(GeoPositionT pos) {
        synchronized (this) {
            positions.add(pos);
            avgLat = (avgLat * samples + pos.getLatitude()) / (samples + 1);
            avgLon = (avgLon * samples + pos.getLongitude()) / (samples + 1);
            samples = positions.size();

            long t = pos.getTimestamp();
            while (!positions.isEmpty() && (t - positions.get(0).getTimestamp()) > PERIOD) {
                GeoPositionT p = positions.get(0);
                avgLat = (avgLat * samples - p.getLatitude()) / (samples - 1);
                avgLon = (avgLon * samples - p.getLongitude()) / (samples - 1);
                positions.remove(0);
                samples = positions.size();
            }
        }
    }

    public Position getAveragePosition() {
        synchronized (this) {
            return new Position(avgLat, avgLon);
        }
    }
}
