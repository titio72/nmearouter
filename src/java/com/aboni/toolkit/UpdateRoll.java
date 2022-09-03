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

package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.utils.LogAdmin;
import com.aboni.utils.ThingsFactory;
import com.aboni.utils.db.DBHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UpdateRoll {

    public void load() {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select id, TS, vMin, v, vMax from meteo where TS>'2022-07-01' and type='ROL'")) {

                if (st.execute()) {
                    try (ResultSet rs = st.getResultSet()) {
                        scanAndUpdate(db, rs);
                    }
                }
            }

        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
    }

    private void scanAndUpdate(DBHelper db, ResultSet rs) throws SQLException {

        long last = 0;
        try (PreparedStatement stUpd = db.getConnection().prepareStatement("delete from meteo where id=?")) {
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(2);
                int id = rs.getInt(1);
                long t = ts.getTime();
                if (last == 0) {
                    // init time
                    last = t;
                } else if ((t - last) < 60000L) {
                    // delete
                    stUpd.setInt(1, id);
                    stUpd.executeUpdate();
                } else {
                    // don't delete
                    System.out.println(ts);
                    last += 60000L;
                }
            }
            db.getConnection().commit();
        }
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        ThingsFactory.getInstance(LogAdmin.class);
        ConfJSON cJ;
        try {
            cJ = new ConfJSON();
            cJ.getLogLevel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new UpdateRoll().load();
    }
}
