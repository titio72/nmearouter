package com.aboni.nmea.router.services;

import java.util.Calendar;

public class DateRangeParameter {

	private final Calendar cFrom;
	private final Calendar cTo;
	
	
	public DateRangeParameter(ServiceConfig config) {
        
        // set today by default
        Calendar c0 = Calendar.getInstance();
        c0.add(Calendar.SECOND, -24*60*60);
        cFrom = config.getParamAsCalendar(config, "date", c0, "yyyyMMddHHmm");
        
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(c0.getTimeInMillis() + 24L*60L*60L*1000L);
        cTo = config.getParamAsCalendar(config, "dateTo", c1, "yyyyMMddHHmm");

	}
	
	public Calendar getFrom() { return cFrom; }
	public Calendar getTo() { return cTo; }
	
	
}
