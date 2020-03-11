package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.*;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;

public class WebServiceFactoryImpl implements WebServiceFactory {

    @Inject
    public WebServiceFactoryImpl() {
        // nothing to initialize
    }

    @Override
    public WebService getService(String target) {
        WebService s = null;
        switch (target) {
            case "/trackanalytics":
                s = ThingsFactory.getInstance(TrackAnalyticsService.class);
                break;
            case "/track":
                s = ThingsFactory.getInstance(TrackService.class);
                break;
            case "/agentsj":
                s = ThingsFactory.getInstance(AgentStatusService.class);
                break;
            case "/shutdown":
                s = ThingsFactory.getInstance(ServiceShutdown.class);
                break;
            case "/sim":
                s = ThingsFactory.getInstance(SimulatorService.class);
                break;
            case "/meteo":
                s = ThingsFactory.getInstance(MeteoService.class);
                break;
            case "/meteo2":
                s = ThingsFactory.getInstance(MeteoService2.class);
                break;
            case "/cruisingdays":
                s = ThingsFactory.getInstance(CruisingDaysService.class);
                break;
            case "/dropcruisingday":
                s = ThingsFactory.getInstance(DropTrackingDayService.class);
                break;
            case "/createtrip":
                s = ThingsFactory.getInstance(CreateTripService.class);
                break;
            case "/changetripdesc":
                s = ThingsFactory.getInstance(ChangeTripDescService.class);
                break;
            case "/dayinfo":
                s = ThingsFactory.getInstance(DayInfoService.class);
                break;
            case "/trips":
                s = ThingsFactory.getInstance(TripStatService.class);
                break;
            case "/speed":
                s = ThingsFactory.getInstance(SpeedService.class);
                break;
            case "/backup":
                s = ThingsFactory.getInstance(ServiceDBBackup.class);
                break;
            case "/filter":
                s = ThingsFactory.getInstance(AgentFilterService.class);
                break;
            case "/auto":
                s = ThingsFactory.getInstance(AutoPilotService.class);
                break;
            default:
                break;
        }
        if (s != null) {
            ServerLog.getLogger().info("ServiceFactory: created {" + s + "}");
        }
        return s;
    }

}
