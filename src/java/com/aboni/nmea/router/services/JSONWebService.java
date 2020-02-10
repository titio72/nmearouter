package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class JSONWebService implements WebService {

    private static final String APPLICATION_JSON = "application/json";
    private WebServiceJSONLoader loader;

    public JSONWebService(@NotNull WebServiceJSONLoader loader) {
        this.loader = loader;
    }

    public JSONWebService() {
        loader = null;
    }

    public void setLoader(@NotNull WebServiceJSONLoader loader) {
        this.loader = loader;
    }

    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        try {
            setResponse(response, getJsonObjectResult(config));
        } catch (Exception e) {
            ServerLog.getLogger().error("Error creating database helper Svc {" + this.getClass().getName() + "}", e);
        }
    }

    private JSONObject getJsonObjectResult(ServiceConfig config) {
        JSONObject res;
        try {
            res = loader.getResult(config);
        } catch (Exception e) {
            ServerLog.getLogger().error("Error reading trip stats", e);
            res = new JSONObject();
            res.put("Error", "Cannot retrieve trips status - check the logs for errors");
        }
        return res;
    }

    private void setResponse(ServiceOutput response, JSONObject res) {
        response.setContentType(APPLICATION_JSON);
        try {
            if (res!=null) {
                response.getWriter().append(res.toString(1));
                response.ok();
            } else {
                response.error("Invalid response detected: something went wrong");
            }
        } catch (IOException e) {
            ServerLog.getLogger().error("Error sending output for Svc {" + this.getClass().getName() + "}", e);
        }
    }

    protected static JSONObject getOk() {
        return getOk("Ok");
    }

    protected static JSONObject getOk(String msg) {
        JSONObject res = new JSONObject();
        res.put("msg", msg);
        return res;
    }

    protected static JSONObject getError(String message) {
        JSONObject res = new JSONObject();
        res.put("error", message);
        return res;
    }
}
