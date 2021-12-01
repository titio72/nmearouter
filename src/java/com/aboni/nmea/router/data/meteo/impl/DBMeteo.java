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

package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.nmea.router.data.meteo.Meteo;
import com.aboni.nmea.router.data.meteo.MeteoManagementException;
import com.aboni.nmea.router.data.meteo.MeteoReader;
import com.aboni.nmea.router.data.meteo.MeteoSample;
import com.aboni.utils.ThingsFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DBMeteo implements Meteo {

    public DBMeteo() {
        // nothing to initialize
    }


    @Override
    public JSONObject getMeteoSeries(@NotNull Instant from, @NotNull Instant to, @NotNull String tag) throws MeteoManagementException {
        MeteoReader m = ThingsFactory.getInstance(MeteoReader.class);
        JSONObject res = new JSONObject();
        m.readMeteo(from, to, tag, (MeteoSample sample) -> addSample(sample, res));
        return res;
    }

    @Override
    public JSONObject getMeteoSeries(@NotNull Instant from, @NotNull Instant to) throws MeteoManagementException {
        MeteoReader m = ThingsFactory.getInstance(MeteoReader.class);
        JSONObject res = new JSONObject();
        m.readMeteo(from, to, (MeteoSample sample) -> addSample(sample, res));
        return res;
    }

    private void addSample(MeteoSample sample, JSONObject res) {
        JSONArray a;
        if (res.has(sample.getTag())) {
            a = res.getJSONArray(sample.getTag());
        } else {
            a = new JSONArray();
            res.put(sample.getTag(), a);
        }
        JSONObject s = new JSONObject();
        s.put("time", sample.getTs());
        s.put("vMin", sample.getMinValue());
        s.put("v", sample.getValue());
        s.put("vMax", sample.getMaxValue());
        a.put(s);
    }
}
