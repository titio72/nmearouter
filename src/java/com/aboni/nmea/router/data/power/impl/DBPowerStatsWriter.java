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

package com.aboni.nmea.router.data.power.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.data.impl.DBStatsWriter;
import com.aboni.utils.Log;
import com.aboni.utils.db.DBEventWriter;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;

public class DBPowerStatsWriter extends DBStatsWriter {

    private final StatsWriter secondary;

    @Inject
    public DBPowerStatsWriter(@NotNull Log log, @NotNull @Named(Constants.TAG_POWER) String tag, @NotNull @Named(Constants.TAG_POWER) DBEventWriter writer) {
        super(log, tag, writer);
        secondary = new DBPowerStatsWriterInflux(log, tag);
    }

    @Override
    public void init() {
        super.init();
        secondary.init();
    }

    @Override
    public void write(StatsSample s, long ts) {
        super.write(s, ts);
        secondary.write(s, ts);
    }

    @Override
    public void dispose() {
        super.dispose();
        secondary.dispose();
    }
}