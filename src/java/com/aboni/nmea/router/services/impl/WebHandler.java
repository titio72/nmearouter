package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.WebService;
import com.aboni.nmea.router.services.WebServiceFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

public class WebHandler extends AbstractHandler {

    private final WebServiceFactory factory;

    @Inject
    public WebHandler(@NotNull WebServiceFactory factory) {
        this.factory = factory;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) {
        WebService s = getService(target);
        if (s != null) {
            s.doIt(new ServletRequestServiceConfig(request),
                    new ServletResponseOutput(response));
            baseRequest.setHandled(true);
        } else {
            baseRequest.setHandled(false);
        }
    }

    private WebService getService(String target) {
        return factory.getService(target);
    }
}