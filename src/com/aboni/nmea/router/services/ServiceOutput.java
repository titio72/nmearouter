package com.aboni.nmea.router.services;

import java.io.IOException;
import java.io.PrintWriter;

public interface ServiceOutput {
	void setContentType(String type);
	PrintWriter getWriter() throws IOException;
	void ok();
	void error(String msg);
    void setHeader(String string, String string2);
}
