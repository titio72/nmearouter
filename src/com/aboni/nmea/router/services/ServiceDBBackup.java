package com.aboni.nmea.router.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.aboni.utils.ServerLog;

public class ServiceDBBackup implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
	    try {
	    	SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
	        ServerLog.getLogger().Info("DB Backup");
	        String file = df.format(new Date()) + ".sql";
	        ProcessBuilder b = new ProcessBuilder("./dbBck.sh", "aboni", "a13928zC", file);
            Process proc = b.start();
            int retCode = proc.waitFor();
            ServerLog.getLogger().Info("DB Backup Return code {" + retCode + "}");
            response.setContentType("application/json");
            JSONObject res = new JSONObject();
            if (retCode==0) {
                res.put("result", "Ok");
            	res.put("file", file + ".tgz");
            } else {
                res.put("result", "Ko");
            	res.put("error", "");
            }
            response.getWriter().print(res.toString());
            response.ok();
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error during db backup", e);
        }
	}
}
