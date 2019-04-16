package com.aboni.nmea.router.services.impl;

import javax.inject.Inject;

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
import com.aboni.nmea.router.services.TrackService;
import com.aboni.nmea.router.services.TripInfoService;
import com.aboni.nmea.router.services.TripStatService;
import com.aboni.nmea.router.services.WebService;
import com.aboni.nmea.router.services.WebServiceFactory;

public class WebServiceFactoryImpl implements WebServiceFactory {

	private final NMEARouter router;
	
	@Inject
	public WebServiceFactoryImpl(NMEARouter router) {
		this.router = router;
	}
	
	@Override
	public WebService getService(String target) {
		WebService s = null;
		switch (target) {
			case "/track":
				s = new TrackService();
				break;
			case "/agentsj":
				s = new AgentStatusService(router);
				break;
			case "/shutdown":
				s = new ServiceShutdown();
				break;
			case "/sim":
				s = new SImulatorService();
				break;
			case "/meteo":
				s = new MeteoService();
				break;
			case "/cruisingdays":
				s = new CruisingDaysService();
				break;
			case "/dropcruisingday":
				s = new DropTrackingDayService();
				break;
			case "/createtrip":
				s = new CreateTripService();
				break;
			case "/changetripdesc":
				s = new ChangeTripDescService();
				break;
			case "/tripinfo":
				s = new TripInfoService();
				break;
			case "/trips":
				s = new TripStatService();
				break;
			case "/speed":
				s = new SpeedService();
				break;
			case "/backup":
				s = new ServiceDBBackup();
				break;
			case "/filterout":
				s = new AgentFilterService(router, "out");
				break;
			case "/filterin":
				s = new AgentFilterService(router, "in");
				break;
			case "/auto":
				s = new AutoPilotService(router);
				break;
			case "/speedanalysis":
				s = new SpeedAnalyticsService();
				break;
		}
		return s;
	}

}
