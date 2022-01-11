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

package com.aboni.nmea.router.data.impl;

import com.aboni.nmea.router.data.StatsEvent;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class DBStatsWriter implements StatsWriter {

    private final Log log;
    private DBHelper db;
    private final DBEventWriter ee;
    private final String tag;

    @Inject
    public DBStatsWriter(@NotNull Log log, @NotNull String tag, @NotNull DBEventWriter statsWriter) {
        this.log = log;
        this.ee = statsWriter;
        this.tag = tag;
    }

    @Override
    public void init() {
        if (db == null) {
            try {
                db = new DBHelper(true);
            } catch (Exception e) {
                LogStringBuilder.start("DBStatsWriter").wV("type", tag).wO("init").error(log, e);
            }
        }
    }

    @Override
    public void write(StatsSample s, long ts) {
        db.write(ee, new StatsEvent(s, ts));
    }

    @Override
    public void dispose() {
        if (db!=null) {
            db.close();
            db = null;
        }
    }
}
