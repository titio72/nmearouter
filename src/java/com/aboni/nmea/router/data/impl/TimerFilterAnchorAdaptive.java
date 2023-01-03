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

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.data.TimerFilter;
import com.aboni.utils.Utils;

import javax.validation.constraints.NotNull;

public class TimerFilterAnchorAdaptive implements TimerFilter {

    private final NMEACache cache;
    private final long period;
    private final long periodAnchor;
    private final long tolerance;

    public TimerFilterAnchorAdaptive(@NotNull NMEACache cache, long period, long periodAnchor) {
        this(cache, period, periodAnchor, 500);
    }

    public TimerFilterAnchorAdaptive(@NotNull NMEACache cache, long period, long periodAnchor, long tolerance) {
        this.cache = cache;
        this.periodAnchor = periodAnchor;
        this.period = period;
        this.tolerance = tolerance;
    }

    @Override
    public boolean accept(long timestamp, long now) {
        long p = cache.getStatus(NMEARouterStatuses.ANCHOR_STATUS, Boolean.FALSE) ? periodAnchor : period;
        return Utils.isNotNewerThan(timestamp, now + tolerance, p);
    }
}
