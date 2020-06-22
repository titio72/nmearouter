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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface Trip {
    int getTrip();

    LocalDate getMinDate();

    LocalDate getMaxDate();

    Instant getStartTS();

    Instant getEndTS();

    String getTripDescription();

    List<LocalDate> getDates();

    double getDistance();

    long getTotalTime();

    int countDays();

    /**
     * Check if the trip was running at the given time
     *
     * @param t The time to be checked
     * @return true is at time t the trip was running, false otherwise.
     */
    boolean checkTimestamp(Instant t);

}
