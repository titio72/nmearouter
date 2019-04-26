package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.services.*;

import javax.inject.Inject;

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
			case "/dayinfo":
				s = new DayInfoService();
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
