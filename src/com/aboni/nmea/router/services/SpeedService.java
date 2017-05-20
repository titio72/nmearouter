package com.aboni.nmea.router.services;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import com.aboni.utils.DBHelper;
import com.aboni.utils.Sample;
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
            Timestamp[] range = db.getTimeframe("track", cFrom, cTo);
            if (range!=null) {
	            long interval = Math.abs(range[0].getTime() - range[1].getTime());
	            interval = interval/150;
	            
	            stm = db.getConnection().prepareStatement("select TS, speed, maxSpeed from track where TS>=? and TS<=?");
	        	stm.setTimestamp(1, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
	        	stm.setTimestamp(2, new java.sql.Timestamp(cTo.getTimeInMillis() ));
	            ResultSet rs = stm.executeQuery();
	            
	            List<Sample> samples = new LinkedList<>();
	            while (rs.next()) {
	                Timestamp ts = rs.getTimestamp(1);
	                double v = rs.getDouble(2);
	                double vMax = rs.getDouble(3);
	                Sample.doSampling(samples, ts.getTime(), vMax, v, v, interval);
	            }
	                
	            fillResponse(response, samples);
            }
        } catch (Exception e) {
            ServerLog.getLogger().Error("Error writing sample", e);
        } finally {
        	try {
				db.close();
			} catch (Exception e2) {}
        }
    }
    
    private void fillResponse(ServiceOutput response, List<Sample> samples) throws IOException {
        boolean first = true;
        response.getWriter().write("{\"serie\":[");
        if (samples!=null) {
	        for (Sample s: samples) {
		        if (!first) {
	                response.getWriter().write(",");
	        	}
	            response.getWriter().write("{\"time\":\"" + new Timestamp(s.t0).toString() + "\",");
	            response.getWriter().write("\"vMin\":" + s.vMin + ",");
	            response.getWriter().write("\"v\":" + s.v + ",");
	            response.getWriter().write("\"vMax\":" + s.vMax + "}");
	            first = false;
	        }
        }
        response.getWriter().write("]}");
    }
}
