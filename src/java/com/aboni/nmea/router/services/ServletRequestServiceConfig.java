package com.aboni.nmea.router.services;

import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

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
        return (res==null)?d:res;
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
    public Calendar getParamAsCalendar(ServiceConfig request, String param, Calendar def, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format);

        String f = request.getParameter(param);
        if (f==null || f.length()==0) {
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
}
