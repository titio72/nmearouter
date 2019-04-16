package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;

public class ServiceShutdown implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
	    try {
	        ServerLog.getLogger().Info("Shutdown");
	        ProcessBuilder b = new ProcessBuilder("./shutdown");
            Process proc = b.start();
            int retCode = proc.waitFor();
            ServerLog.getLogger().Info("Shutdown Return code {" + retCode + "}");
            response.setContentType("text/plain;charset=utf-8");
            response.getWriter().print((retCode==0)?"Ok":"Ko");
            response.ok();
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error during shutdown", e);
        }
	}
}
