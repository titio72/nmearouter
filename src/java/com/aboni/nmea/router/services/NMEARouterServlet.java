package com.aboni.nmea.router.services;

import com.aboni.nmea.router.services.impl.ServletRequestServiceConfig;
import com.aboni.nmea.router.services.impl.ServletResponseOutput;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NMEARouterServlet<T extends WebService> extends HttpServlet {

    private final T service;

    public NMEARouterServlet(T service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        service.doIt(new ServletRequestServiceConfig(req), new ServletResponseOutput(resp));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        this.doGet(req, resp);
    }
}
