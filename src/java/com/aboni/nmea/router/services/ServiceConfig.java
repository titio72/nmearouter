package com.aboni.nmea.router.services;

import java.util.Calendar;

public interface ServiceConfig {
    String getParameter(String pnamne);
    String getParameter(String pnamne, String defaultValue);

    int getInteger(String pname, int defaultVale);

    Calendar getParamAsCalendar(ServiceConfig request, String param, Calendar def, String format);
}
