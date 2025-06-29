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

package com.aboni.nmea.router.data.impl;

import com.aboni.nmea.router.data.*;
import com.aboni.nmea.router.data.sampledquery.SampleWriter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;

public abstract class DBSeriesReader implements SeriesReader {

    final SampleWriter defaultWriter = new DefaultSampleWriter();

    protected abstract DataReader getNewDataReader();

    @Override
    public JSONObject getSeries(Instant from, Instant to, String tag) throws DataManagementException {
        if (from==null || to==null || tag==null || from.isAfter(to)) throw new IllegalArgumentException("Invalid series arguments");
        DataReader m = getNewDataReader();
        JSONObject res = new JSONObject();
        m.readData(new QueryByDate(from, to), tag, (Sample sample) -> addSample(sample, res));
        return res;
    }

    @Override
    public JSONObject getSeries(Instant from, Instant to) throws DataManagementException {
        if (from==null || to==null || from.isAfter(to)) throw new IllegalArgumentException("Invalid series arguments");
        DataReader m = getNewDataReader();
        JSONObject res = new JSONObject();
        m.readData(new QueryByDate(from, to), (Sample sample) -> addSample(sample, res));
        return res;
    }

    private void addSample(Sample sample, JSONObject res) {
        JSONArray a;
        if (res.has(sample.getTag())) {
            a = res.getJSONArray(sample.getTag());
        } else {
            a = new JSONArray();
            res.put(sample.getTag(), a);
        }
        a.put(defaultWriter.getSampleNode(sample));
    }
}
