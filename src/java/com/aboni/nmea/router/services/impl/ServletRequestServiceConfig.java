package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.ServiceConfig;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

public class ServletRequestServiceConfig implements ServiceConfig {

	private final ServletRequest r;
	
	public ServletRequestServiceConfig(ServletRequest r) {
		this.r = r;
    }

    @Override
    public String getParameter(String pnamne) {
        return r.getParameter(pnamne);
    }

    @Override
    public String getParameter(String pnamne, String d) {
        String res = r.getParameter(pnamne);
        return (res == null) ? d : res;
    }

    @Override
    public int getInteger(String pname, int defaultValue) {
        String p = getParameter(pname);
        if (p!=null) {
            try {
                return Integer.parseInt(p);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String pname, double defaultValue) {
        String p = getParameter(pname);
        if (p!=null) {
            try {
                return Double.parseDouble(p);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    @Override
    public Calendar getParamAsDate(String param, int dayOffset) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(c.getTimeInMillis() - (c.getTimeInMillis() % (24 * 60 * 60 * 1000)));
        c = getParamAsCalendar(param, c, "yyyyMMdd");
        c.add(Calendar.HOUR, 24 * dayOffset);
        return c;
    }

    @Override
    public Calendar getParamAsCalendar(String param, Calendar def, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);

        String f = getParameter(param);
        if (f == null || f.length() == 0) {
            return def;
        }
        try {
            Date d = df.parse(f);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            return c;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String dump() {
        StringBuilder b = new StringBuilder();
        Enumeration<String> e = r.getParameterNames();
        while (e.hasMoreElements()) {
            String p = e.nextElement();
            b.append(" ").append(p).append(" {").append(r.getParameter(p)).append("}");
        }
        if (b.length() > 0) {
            return b.substring(1);
        } else {
            return "";
        }


    }

}
