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

import com.aboni.nmea.router.utils.db.Event;

public class TripEvent implements Event {

    private final Trip t;

    public TripEvent(Trip t) {
        if (t==null) throw new IllegalArgumentException("Trip is null");
        this.t = t;
    }

    public Trip getTrip() {
        return t;
    }

    @Override
    public long getTime() {
        return t.getEndTS().toEpochMilli();
    }
}
