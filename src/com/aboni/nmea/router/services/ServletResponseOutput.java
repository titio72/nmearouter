package com.aboni.nmea.router.services;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

public class ServletResponseOutput implements ServiceOutput {

	private final HttpServletResponse r;
	
	public ServletResponseOutput(HttpServletResponse r) {
		this.r = r;
	}
	
	@Override
	public void setContentType(String type) {
		r.setContentType(type);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return r.getWriter();
	}

	@Override
	public void ok() {
        r.setStatus(HttpServletResponse.SC_OK);
	}

	@Override
	public void error(String msg) throws IOException {
        r.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        r.getWriter().write(msg);
	}

    @Override
    public void setHeader(String string, String string2) {
        r.setHeader(string, string2);
    }
}
