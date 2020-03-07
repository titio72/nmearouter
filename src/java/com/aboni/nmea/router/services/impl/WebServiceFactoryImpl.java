package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.services.*;
import com.aboni.nmea.router.track.impl.DBTrackQueryManager;
import com.aboni.nmea.router.track.impl.DBTripManager;
import com.aboni.utils.ServerLog;

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
            case "/trackanalytics":
                s = new TrackAnalyticsService(new DBTripManager());
                break;
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
                s = new SimulatorService();
                break;
            case "/meteo":
                s = new MeteoService();
                break;
            case "/meteo2":
                s = new MeteoService2();
                break;
            case "/cruisingdays":
                s = new CruisingDaysService(new DBTrackQueryManager());
                break;
            case "/dropcruisingday":
                s = new DropTrackingDayService(new DBTrackQueryManager());
                break;
            case "/createtrip":
                s = new CreateTripService(new DBTripManager());
                break;
            case "/changetripdesc":
                s = new ChangeTripDescService(new DBTripManager());
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
            case "/distanalysis":
                s = new DistanceAnalyticsService(new DBTrackQueryManager());
                break;
            default:
                break;
        }
        if (s != null) {
            ServerLog.getLogger().info("ServiceFactory {" + s + "}");
        }
        return s;
    }

}
