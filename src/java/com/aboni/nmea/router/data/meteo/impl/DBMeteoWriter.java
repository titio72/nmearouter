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

package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.StatsWriter;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.db.DBEventWriter;
import com.aboni.utils.db.DBHelper;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class DBMeteoWriter implements StatsWriter {

    private final Log log;
    private DBHelper db;
    private final DBEventWriter ee;

    @Inject
    public DBMeteoWriter(@NotNull Log log) {
        this.log = log;
        ee = new DBMeteoEventWriter();
    }

    @Override
    public void init() {
        if (db == null) {
            try {
                db = new DBHelper(true);
            } catch (Exception e) {
                LogStringBuilder.start("DBMeteoWriter").wO("init").error(log, e);
            }
        }
    }

    @Override
    public void write(StatsSample s, long ts) {
        db.write(ee, new MeteoEvent(s, ts));
    }

    @Override
    public void dispose() {
        if (db!=null) {
            db.close();
            db = null;
        }
    }
}
