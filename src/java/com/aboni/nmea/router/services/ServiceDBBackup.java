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
                res.put("file", file + ".tgz");
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
