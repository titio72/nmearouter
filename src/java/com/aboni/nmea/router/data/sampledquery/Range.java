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

package com.aboni.nmea.router.data.sampledquery;

import java.time.Instant;

public class Range {
    private final Instant max;
    private final Instant min;
    private final long count;

    public Range(Instant max, Instant min, long count) {
        this.max = max;
        this.min = min;
        this.count = count;
    }

    public long getCount() {
        return count;
    }

    public long getInterval() {
        return max.toEpochMilli() - min.toEpochMilli();
    }

    public int getSampling(int maxSamples) {
        return (int) ((getCount() <= maxSamples) ? 1 : (getInterval() / maxSamples));
    }

    public Instant getMax() {
        return max;
    }

    public Instant getMin() {
        return min;
    }

}
