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

package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

public class ServiceDBBackup extends JSONWebService {

    public ServiceDBBackup() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        try (DBHelper h = new DBHelper(true)) {
            String file = h.backup();
            ServerLog.getLogger().info("DB Backup Return {" + file + "}");
            if (file != null) {
                JSONObject res = getOk();
                res.put("file", file);
                return res;
            } else {
                return getError("Backup failed");
            }
        } catch (Exception e) {
            ServerLog.getLogger().error("Error during db backup", e);
            return getError("Error " + e.getMessage());
        }
    }
}
