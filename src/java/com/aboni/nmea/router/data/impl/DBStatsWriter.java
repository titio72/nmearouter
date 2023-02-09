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

import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.data.StatsEvent;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.SafeLog;
import com.aboni.nmea.router.utils.db.DBEventWriter;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;

public class DBStatsWriter implements StatsWriter {

    private final Log log;
    private DBHelper db;
    private final DBEventWriter ee;
    private final String tag;

    @Inject
    public DBStatsWriter(Log log, String tag, DBEventWriter statsWriter) {
        this.log = SafeLog.getSafeLog(log);
        if (tag==null || statsWriter==null) throw new IllegalArgumentException("Invalid argument: tag and writers cannot be null");
        this.ee = statsWriter;
        this.tag = tag;
    }

    @Override
    public void init() {
        if (db == null) {
            try {
                db = new DBHelper(log, true);
            } catch (Exception e) {
                log.error(() -> LogStringBuilder.start("DBStatsWriter").wV("type", tag).wO("init").toString(), e);
            }
        }
    }

    @Override
    public void write(Sample s, long ts) {
        if (db != null) db.write(ee, new StatsEvent(s, ts));
    }

    @Override
    public void dispose() {
        if (db!=null) {
            db.close();
            db = null;
        }
    }
}
