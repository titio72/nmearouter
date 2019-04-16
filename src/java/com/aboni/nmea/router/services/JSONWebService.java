package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.io.IOException;

public abstract class JSONWebService implements WebService {

    private static final String APPLICATION_JSON = "application/json";
    private DBHelper db;

    protected abstract JSONObject getResult(ServiceConfig config, DBHelper db);

    private DBHelper getHelper() {
        if (db==null) {
            try {
                db = new DBHelper(true);
            } catch (ClassNotFoundException e) {
                ServerLog.getLogger().Error("Error creating database helper Svc {" + this.getClass().getName() + "}", e);
            }
        }
        return db;
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {

        DBHelper db = getHelper();
        if (db!=null) {
            JSONObject res;
            try {
                res = getResult(config, db);
            } catch (Exception e) {
                ServerLog.getLogger().Error("Error reading trip stats", e);
                res = new JSONObject();
                res.put("Error", "Cannot retrieve trips status - check the logs for errors");
            }

            response.setContentType(APPLICATION_JSON);
            try {
                response.getWriter().append(res.toString());
                response.ok();
            } catch (IOException e) {
                ServerLog.getLogger().Error("Error sending output for Svc {" + this.getClass().getName() + "}", e);
            }
            db.close();
        }
    }
}
