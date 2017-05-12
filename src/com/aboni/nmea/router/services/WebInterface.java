package com.aboni.nmea.router.services;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebInterface extends AbstractHandler
{
    
    public WebInterface() {
    }
    
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
		WebService s = null;
        if (target.equals("/track")) {
            s = new TrackService();
        } else if (target.equals("/agents")) {
            s = new AgentStatusService();
        } else if (target.equals("/agentsj")) {
            s = new AgentStatusServiceJSON();
        } else if (target.equals("/sensor")) {
        	s = new StatusService();
        } else if (target.equals("/shutdown")) {
        	s = new ServiceShutdown();
        } else if (target.equals("/sim")) {
        	s = new SImulatorService();
        } else if (target.equals("/meteo")) {
        	s = new MeteoService();
        } else if (target.equals("/cruisingdays")) {
        	s = new CruisingDaysService();
        } else if (target.equals("/dropcruisingday")) {
        	s = new DropTrackingDayService();
        } else if (target.equals("/createtrip")) {
        	s = new CreateTripService();
        } else if (target.equals("/changetripdesc")) {
        	s = new ChangeTripDescService();
        } else if (target.equals("/tripinfo")) {
        	s = new TripInfoService();
        } else if (target.equals("/speed")) {
        	s = new SpeedService();
        }
		return s;
	}
}
