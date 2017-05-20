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

public class MeteoService implements WebService {

	private DBHelper db;
    private PreparedStatement stm;
    
    public MeteoService() {
    }
    
    @Override
    public void doIt(ServiceConfig config, ServiceOutput response) {
        String type = config.getParameter("type", "PR_");

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
            Timestamp[] range = db.getTimeframe("meteo", cFrom, cTo);
            if (range!=null) {
	            long interval = Math.abs(range[0].getTime() - range[1].getTime());
	            interval = interval/150;
	            
	            stm = db.getConnection().prepareStatement("select TS, vMax, v, vMin from meteo where type=? and TS>=? and TS<=?");
	            stm.setString(1, type);
	        	stm.setTimestamp(2, new java.sql.Timestamp(cFrom.getTimeInMillis() ));
	        	stm.setTimestamp(3, new java.sql.Timestamp(cTo.getTimeInMillis() ));
	            ResultSet rs = stm.executeQuery();
	            
	            List<Sample> samples = new LinkedList<>();
	            while (rs.next()) {
	                Timestamp ts = rs.getTimestamp(1);
	                double vMax = rs.getDouble(2);
	                double v = rs.getDouble(3);
	                double vMin = rs.getDouble(4);
	                Sample.doSampling(samples, ts.getTime(), vMax, v, vMin, interval);
	            }
	            fillResponse(response, type, samples);
            }
        } catch (Exception e) {
        	e.printStackTrace();
            ServerLog.getLogger().Error("Error writing sample", e);
        } finally {
        	try {
				db.close();
			} catch (Exception e2) {}
        }
    }

	private void fillResponse(ServiceOutput response, String type, List<Sample> samples) throws IOException {
		response.getWriter().write("{\"type\":\""+ type +"\", \"serie\":[");
		boolean first = true;
        if (samples!=null) {
			for (Sample s: samples) {
			    Timestamp ts = new Timestamp(s.t0);
			    double vMax = s.vMax;
			    double v = s.v;
			    double vMin = s.vMin;
				
				if (!first) {
			        response.getWriter().write(",");
				}
			    response.getWriter().write("{\"time\":\"" + ts.toString() + "\",");
			    response.getWriter().write("\"vMin\":" + vMin + ",");
			    response.getWriter().write("\"v\":" + v + ",");
			    response.getWriter().write("\"vMax\":" + vMax + "}");
			    first = false;
			}
        }
		response.getWriter().write("]}");
	}
}
