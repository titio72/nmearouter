package com.aboni.nmea.router.services;

import org.json.JSONObject;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

public class ServiceDBBackup implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
    	DBHelper h = null;
	    try {
	    	h = new DBHelper(true);
	    	String file = h.backup();
            ServerLog.getLogger().Info("DB Backup Return {" + file + "}");
            response.setContentType("application/json");
            JSONObject res = new JSONObject();
            if (file!=null) {
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
        } finally {
        	if (h!=null) {
				h.close();
        	}
        }
	}
}
