package com.aboni.nmea.router.services.impl;

import javax.inject.Inject;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.services.AgentFilterService;
import com.aboni.nmea.router.services.AgentStatusService;
import com.aboni.nmea.router.services.AutoPilotService;
import com.aboni.nmea.router.services.ChangeTripDescService;
import com.aboni.nmea.router.services.CreateTripService;
import com.aboni.nmea.router.services.CruisingDaysService;
import com.aboni.nmea.router.services.DropTrackingDayService;
import com.aboni.nmea.router.services.MeteoService;
import com.aboni.nmea.router.services.SImulatorService;
import com.aboni.nmea.router.services.ServiceDBBackup;
import com.aboni.nmea.router.services.ServiceShutdown;
import com.aboni.nmea.router.services.SpeedAnalyticsService;
import com.aboni.nmea.router.services.SpeedService;
import com.aboni.nmea.router.services.StatusService;
import com.aboni.nmea.router.services.TrackService;
import com.aboni.nmea.router.services.TripInfoService;
import com.aboni.nmea.router.services.TripStatService;
import com.aboni.nmea.router.services.WebService;
import com.aboni.nmea.router.services.WebServiceFactory;

public class WebServiceFactoryImpl implements WebServiceFactory {

	private NMEACache cache;
	private NMEARouter router;
	
	@Inject
	public WebServiceFactoryImpl(NMEACache cache, NMEARouter router) {
		this.cache = cache;
		this.router = router;
	}
	
	@Override
	public WebService getService(String target) {
		WebService s = null;
        if (target.equals("/track")) {
            s = new TrackService();
        } else if (target.equals("/agentsj")) {
            s = new AgentStatusService(router);
        } else if (target.equals("/sensor")) {
        	s = new StatusService(cache);
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
        } else if (target.equals("/trips")) {
        	s = new TripStatService();
        } else if (target.equals("/speed")) {
        	s = new SpeedService();
        } else if (target.equals("/backup")) {
        	s = new ServiceDBBackup();
        } else if (target.equals("/filterout")) {
        	s = new AgentFilterService(router, "out");
        } else if (target.equals("/filterin")) {
        	s = new AgentFilterService(router, "in");
        } else if (target.equals("/auto")) {
        	s = new AutoPilotService(router);
        } else if (target.equals("/speedanalysis")) {
        	s = new SpeedAnalyticsService();
        }
		return s;
	}

}
