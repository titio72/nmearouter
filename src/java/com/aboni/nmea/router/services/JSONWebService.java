package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONObject;

import java.io.IOException;

public abstract class JSONWebService implements WebService {

    private static final String APPLICATION_JSON = "application/json";

    protected abstract JSONObject getResult(ServiceConfig config, DBHelper db);

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        try (DBHelper db = new DBHelper(true)) {
            setResponse(response, getJsonObjectResult(config, db));
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error creating database helper Svc {" + this.getClass().getName() + "}", e);
        }
    }

    private JSONObject getJsonObjectResult(ServiceConfig config, DBHelper db) {
        JSONObject res;
        try {
            res = getResult(config, db);
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error reading trip stats", e);
            res = new JSONObject();
            res.put("Error", "Cannot retrieve trips status - check the logs for errors");
        }
        return res;
    }

    private void setResponse(ServiceOutput response, JSONObject res) {
        response.setContentType(APPLICATION_JSON);
        try {
            if (res!=null) {
                response.getWriter().append(res.toString());
                response.ok();
            } else {
                response.error("Invalid response detected: something went wrong");
            }
        } catch (IOException e) {
            ServerLog.getLogger().Error("Error sending output for Svc {" + this.getClass().getName() + "}", e);
        }
    }
}
