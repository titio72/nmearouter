package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import org.json.JSONObject;

public class ServiceShutdown extends JSONWebService {

    public ServiceShutdown() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
	    try {
	        ServerLog.getLogger().info("Shutdown");
	        ProcessBuilder b = new ProcessBuilder("./shutdown");
            Process proc = b.start();
            int retCode = proc.waitFor();
            ServerLog.getLogger().info("Shutdown Return code {" + retCode + "}");
            return getOk();
        } catch (Exception e) {
            ServerLog.getLogger().error("Error during shutdown", e);
            return getError("Error " + e.getMessage());
        }
	}
}
