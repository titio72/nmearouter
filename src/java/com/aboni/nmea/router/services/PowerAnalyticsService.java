/*
 * Copyright (c) 2022,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.power.PowerAnalytics;
import com.aboni.nmea.router.data.power.impl.DBPowerAnalytics;
import com.aboni.nmea.router.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class PowerAnalyticsService extends JSONWebService {

    @Inject
    public PowerAnalyticsService(@NotNull Log log) {
        super(log);
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        PowerAnalytics analytics = new DBPowerAnalytics();
        Instant from = config.getParamAsInstant("dateFrom", Instant.now().minusSeconds(86400L), 0);
        Instant to = config.getParamAsInstant("dateTo", Instant.now(), 1);
        int period = config.getInteger("period", 3600);
        try {
            return analytics.getPowerUsage(period, from, to);
        } catch (DataManagementException e) {
            throw new JSONGenerationException("Error loading meteo data", e);
        }
    }
}
