package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.ServiceConfig;

import javax.servlet.ServletRequest;
import java.util.Enumeration;

public class ServletRequestServiceConfig implements ServiceConfig {

	private final ServletRequest r;
	
	public ServletRequestServiceConfig(ServletRequest r) {
		this.r = r;
    }

    @Override
    public String getParameter(String name) {
        return r.getParameter(name);
    }

    @Override
    public String getParameter(String name, String d) {
        String res = r.getParameter(name);
        return (res == null) ? d : res;
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
