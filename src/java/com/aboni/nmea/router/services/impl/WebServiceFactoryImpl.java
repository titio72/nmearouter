/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

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
            case "/ais":
                s = ThingsFactory.getInstance(AISTargetsService.class);
                break;
            case "/gps":
                s = ThingsFactory.getInstance(GPSStatusService.class);
                break;
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
            case "/changetripdesc":
                s = ThingsFactory.getInstance(ChangeTripDescService.class);
                break;
            case "/droptrip":
                s = ThingsFactory.getInstance(DropTripService.class);
                break;
            case "/trips":
                s = ThingsFactory.getInstance(TripListService.class);
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
            case "/distanalysis":
                s = ThingsFactory.getInstance(YearlyAnalyticsService.class);
                break;
            default:
                break;
        }
        if (s != null) {
            ServerLog.getLogger().debug("ServiceFactory: created {" + s + "}");
        }
        return s;

    }

}
