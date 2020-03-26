package com.aboni.nmea.router.data.sampledquery;

import com.aboni.utils.TimeSeriesSample;
import org.json.JSONObject;

public interface SampleWriter {
    JSONObject[] getSampleNode(TimeSeriesSample sample);
}
