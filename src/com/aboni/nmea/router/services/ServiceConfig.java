package com.aboni.nmea.router.services;

import java.util.Calendar;

public interface ServiceConfig {
    String getParameter(String pnamne);
    String getParameter(String pnamne, String defaultValue);
    
    Calendar getParamAsCalendar(ServiceConfig request, String param, Calendar def, String format);
}
