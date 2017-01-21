package com.aboni.nmea.router.services;

import java.sql.PreparedStatement;
import java.util.Calendar;

import com.aboni.utils.DBHelper;

public class DropTrackingDayService implements WebService {

	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
		DBHelper db = null;
		try {
	        response.setContentType("text/plain;charset=utf-8");
            db = new DBHelper(true);
            PreparedStatement stm = db.getConnection().prepareStatement("delete from track where Date(TS)=?");
        	Calendar cDate = config.getParamAsCalendar(config, "date", null, "yyyyMMdd");
        	if (cDate!=null) {
	        	stm.setDate(1, new java.sql.Date(cDate.getTimeInMillis()));
	            stm.executeUpdate();
        	}
        	response.getWriter().append("Ok");
            response.ok();

		} catch (Exception e) {
            response.setContentType("text/plain;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());
		} finally {
			try {
				db.close();
			} catch (Exception e2) {}
		}
	}
}
