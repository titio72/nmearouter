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

import com.aboni.nmea.router.data.HistoryProvider;
import com.aboni.nmea.router.data.Sample;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.data.metrics.Metric;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.data.sampledquery.SampleWriter;
import com.aboni.nmea.router.data.sampledquery.SamplesQueryToJSON;
import com.aboni.log.Log;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;

public class MeteoRollingWindowService extends JSONWebService {

    private final NMEARouter router;
    private HistoryProvider meteoTarget;

    @Inject
    public MeteoRollingWindowService(NMEARouter router, Log log) {
        super(log);
        if (router==null) throw new IllegalArgumentException("NMEARouter is null");
        this.router = router;
        setLoader((ServiceConfig c) -> getJSON(c.getParameter("type", "PR_")));
    }

    private static class SimpleWriter implements SampleWriter {
        @Override
        public JSONObject[] getSampleNode(Sample sample) {
            if (sample != null && !Double.isNaN(sample.getValue())) {
                JSONObject res = new JSONObject();
                res.put("x", sample.getTimestamp());
                res.put("y", sample.getValue());
                return new JSONObject[]{res};
            } else {
                return new JSONObject[]{};
            }
        }
    }

    private static final SimpleWriter WRITER = new SimpleWriter();

    private JSONObject getJSON(String type) {
        SamplesQueryToJSON toJSON = new SamplesQueryToJSON(s -> WRITER);
        Metric i;
        switch (type) {
            case "HUM":
                i = Metrics.HUMIDITY;
                break;
            case "AT0":
                i = Metrics.AIR_TEMPERATURE;
                break;
            case "WT_":
                i = Metrics.WATER_TEMPERATURE;
                break;
            case "TW_":
                i = Metrics.WIND_SPEED;
                break;
            case "TWD":
                i = Metrics.WIND_DIRECTION;
                break;
            default:
                i = Metrics.PRESSURE;

        }
        HistoryProvider meteoProvider = findService();
        if (meteoProvider != null) {
            List<Sample> series = meteoProvider.getHistory(i);
            toJSON.fillResponse(type, series);
            return toJSON.getResult();
        }

        return getError("Cannot find a suitable meteo provider");
    }

    private HistoryProvider findService() {
        if (meteoTarget == null) {
            for (String ag_id : router.getAgents()) {
                NMEAAgent ag = router.getAgent(ag_id);
                if (ag instanceof HistoryProvider) {
                    meteoTarget = (HistoryProvider) ag;
                    break;
                }
            }
        }
        return meteoTarget;
    }
}
