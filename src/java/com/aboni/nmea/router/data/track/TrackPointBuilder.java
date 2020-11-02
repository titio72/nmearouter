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
import com.aboni.sensors.EngineStatus;

public interface TrackPointBuilder {

    TrackPointBuilder getNew();

    TrackPointBuilder withPoint(TrackPoint point);

    TrackPointBuilder withPosition(GeoPositionT pos);

    TrackPointBuilder withSpeed(double speed, double maxSpeed);

    TrackPointBuilder withAnchor(boolean anchor);

    TrackPointBuilder withDistance(double distance);

    TrackPointBuilder withPeriod(int period);

    TrackPointBuilder withEngine(EngineStatus engine);

    TrackPoint getPoint();
}
