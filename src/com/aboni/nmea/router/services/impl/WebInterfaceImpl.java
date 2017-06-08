package com.aboni.nmea.router.services.impl;

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import com.aboni.nmea.router.services.ServletRequestServiceConfig;
import com.aboni.nmea.router.services.ServletResponseOutput;
import com.aboni.nmea.router.services.WebService;
import com.aboni.nmea.router.services.WebServiceFactory;

public class WebInterfaceImpl extends AbstractHandler
{
    
	private WebServiceFactory factory;
	
	@Inject
    public WebInterfaceImpl(WebServiceFactory factory) {
		this.factory = factory;
    }
    
    /* (non-Javadoc)
	 * @see com.aboni.nmea.router.services.WebInterface#handle(java.lang.String, org.eclipse.jetty.server.Request, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) 
        throws IOException, ServletException
    {
    	WebService s = getService(target);
    	if (s!=null) {
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
