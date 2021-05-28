/*
 * Copyright (c) 2020,  Andrea Boni
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

import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.data.StatsSample;
import com.aboni.nmea.router.data.meteo.MeteoHistory;
import com.aboni.nmea.router.data.meteo.MeteoMetrics;
import com.aboni.nmea.router.data.sampledquery.SampleWriter;
import com.aboni.nmea.router.data.sampledquery.SamplesQueryToJSON;
import com.aboni.utils.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;

public class MeteoRollingWindowService extends JSONWebService {

    private final NMEARouter router;
    private MeteoHistory meteoTarget;

    @Inject
    public MeteoRollingWindowService(@NotNull NMEARouter router, @NotNull Log log) {
        super(log);
        this.router = router;
        setLoader((ServiceConfig c) -> getJSON(c.getParameter("type", "PR_")));
    }

    private static class SimpleWriter implements SampleWriter {
        @Override
        public JSONObject[] getSampleNode(StatsSample sample) {
            JSONObject res = new JSONObject();
            res.put("x", sample.getT1());
            res.put("y", sample.getAvg());
            return new JSONObject[]{res};
        }
    }

    private static final SimpleWriter WRITER = new SimpleWriter();

    private JSONObject getJSON(String type) {
        SamplesQueryToJSON toJSON = new SamplesQueryToJSON(s -> WRITER);
        MeteoMetrics i;
        switch (type) {
            case "HUM":
                i = MeteoMetrics.HUMIDITY;
                break;
            case "AT0":
                i = MeteoMetrics.AIR_TEMPERATURE;
                break;
            case "WT_":
                i = MeteoMetrics.WATER_TEMPERATURE;
                break;
            case "TW_":
                i = MeteoMetrics.WIND_SPEED;
                break;
            case "TWD":
                i = MeteoMetrics.WIND_DIRECTION;
                break;
            default:
                i = MeteoMetrics.PRESSURE;

        }
        MeteoHistory meteoProvider = findService();
        if (meteoProvider != null) {
            List<StatsSample> series = meteoProvider.getHistory(i);
            toJSON.fillResponse(type, series);
            return toJSON.getResult();
        }

        return getError("Cannot find a suitable meteo provider");
    }

    private MeteoHistory findService() {
        if (meteoTarget == null) {
            for (String ag_id : router.getAgents()) {
                NMEAAgent ag = router.getAgent(ag_id);
                if (ag instanceof MeteoHistory) {
                    meteoTarget = (MeteoHistory) ag;
                    break;
                }
            }
        }
        return meteoTarget;
    }
}
