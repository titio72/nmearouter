package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.impl.JSONFilterUtils;
import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public class DummyFilter implements NMEAFilter {

    public static final String FILTER_TYPE = "dummy";
    private static final String FILTER = "filter";
    private final String data;

    public DummyFilter(String data) {
        this.data = data;
    }

    public static DummyFilter parseFilter(JSONObject obj) {
        obj = JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        return new DummyFilter(
                JSONUtils.getAttribute(obj, "data", "")
        );
    }

    public String getData() {
        return data;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        JSONObject fltObj = new JSONObject();
        obj.put(FILTER, fltObj);
        fltObj.put("type", FILTER_TYPE);
        fltObj.put("data", getData());
        return obj;
    }

    @Override
    public boolean match(RouterMessage m) {
        return true;
    }

    @Override
    public String toString() {
        return data;
    }
}
