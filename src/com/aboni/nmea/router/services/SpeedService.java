package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

import com.aboni.utils.DBHelper;
import com.aboni.utils.ServerLog;

public class SpeedService implements WebService {

    private DBHelper db;
    private PreparedStatement stm;
    
    public SpeedService() {
    }
    
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        response.setContentType("application/json");
        // set today by default
        Calendar c0 = Calendar.getInstance();
        c0.add(Calendar.SECOND, -24*60*60);
        Calendar cFrom = config.getParamAsCalendar(config, "date", c0, "yyyyMMddHHmm");
        
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(c0.getTimeInMillis() + 24L*60L*60L*1000L);
        Calendar cTo = config.getParamAsCalendar(config, "dateTo", c1, "yyyyMMddHHmm");
        
        try {
            db = new DBHelper(true);
            stm = db.getConnection().prepareStatement("select TS, speed, maxSpeed from track where TS>=? and TS<=?");

        	stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
        	stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
            ResultSet rs = stm.executeQuery();
            
            response.getWriter().write("{\"serie\":[");
            
            boolean first = true;
            
            while (rs.next()) {
            	if (!first) {
                    response.getWriter().write(",");
            	}
                Timestamp ts = rs.getTimestamp(1);
                double v = rs.getDouble(2);
                double vMax = rs.getDouble(3);
                response.getWriter().write("{\"time\":\"" + ts.toString() + "\",");
                response.getWriter().write("\"v\":" + v + ",");
                response.getWriter().write("\"vMax\":" + vMax + "}");
                first = false;
            }
            response.getWriter().write("]}");
        
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error writing sample", e);
        } finally {
        	try {
				db.close();
			} catch (Exception e2) {}
        }
    }

}
