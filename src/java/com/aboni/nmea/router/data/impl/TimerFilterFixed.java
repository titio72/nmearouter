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

package com.aboni.nmea.router.data.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.data.TimerFilter;

public class TimerFilterFixed implements TimerFilter {

    private final long period;
    private final long tolerance;

    public TimerFilterFixed(long periodInMs, long tolerance) {
        this.period = periodInMs;
        this.tolerance = tolerance;
    }

    public TimerFilterFixed(long periodInMs) {
        this(periodInMs, 0);
    }

    @Override
    public boolean accept(long timestamp, long now) {

        return Utils.isNotNewerThan(timestamp, now + tolerance, period);
    }
}
