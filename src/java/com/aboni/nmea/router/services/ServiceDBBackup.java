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

import com.aboni.log.Log;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.log.LogStringBuilder;
import org.json.JSONObject;

import javax.inject.Inject;

public class ServiceDBBackup extends JSONWebService {

    public static final String DB_BACKUP_SERVICE = "DBBackupService";
    public static final String OPERATION = "backup";

    @Inject
    public ServiceDBBackup(Log log) {
        super(log);
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        try (DBHelper h = new DBHelper(getLogger(), true)) {
            String file = h.backup();
            getLogger().info(LogStringBuilder.start(DB_BACKUP_SERVICE).wO(OPERATION).wV("file", file).toString());
            if (file != null) {
                JSONObject res = getOk();
                res.put("file", file);
                return res;
            } else {
                return getError("Backup failed");
            }
        } catch (InterruptedException e) {
            getLogger().errorForceStacktrace(LogStringBuilder.start(DB_BACKUP_SERVICE).wO(OPERATION).toString(), e);
            Thread.currentThread().interrupt();
            return getError("Error " + e.getMessage());
        } catch (Exception e) {
            getLogger().errorForceStacktrace(LogStringBuilder.start(DB_BACKUP_SERVICE).wO(OPERATION).toString(), e);
            return getError("Error " + e.getMessage());
        }
    }
}
