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

package com.aboni.nmea.router.services;

import com.aboni.nmea.router.data.track.TrackQueryManager;
import com.aboni.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class YearlyAnalyticsService extends JSONWebService {

    private final TrackQueryManager trackQueryManager;

    @Inject
    public YearlyAnalyticsService(@NotNull final TrackQueryManager manager, @NotNull Log log) {
        super(log);
        this.trackQueryManager = manager;
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) throws JSONGenerationException {
        try {
            return trackQueryManager.getYearlyStats();
        } catch (Exception e) {
            throw new JSONGenerationException("Error reading yearly stats", e);
        }
    }
}