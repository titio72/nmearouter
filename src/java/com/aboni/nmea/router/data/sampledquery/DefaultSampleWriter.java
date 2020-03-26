package com.aboni.nmea.router.data.sampledquery;

import com.aboni.misc.Utils;
import com.aboni.utils.TimeSeriesSample;
import org.json.JSONObject;

class DefaultSampleWriter implements SampleWriter {
    @Override
    public JSONObject[] getSampleNode(TimeSeriesSample sample) {
        JSONObject s = new JSONObject();
        s.put("time", sample.getT0());
        s.put("vMin", Utils.round(sample.getValueMin(), 2));
        s.put("v", Utils.round(sample.getValue(), 2));
        s.put("vMax", Utils.round(sample.getValueMax(), 2));
        return new JSONObject[]{s};
    }
}
