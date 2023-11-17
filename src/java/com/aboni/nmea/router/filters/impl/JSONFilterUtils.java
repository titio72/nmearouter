package com.aboni.nmea.router.filters.impl;

import com.aboni.data.Pair;
import org.json.JSONObject;

public class JSONFilterUtils {

    private static final String FILTER_TAG_NAME = "filter";
    private static final String FILTER_TYPE_TAG_NAME = "type";

    private JSONFilterUtils() {}

    public static Pair<JSONObject, String> getFilter(JSONObject obj) {
        if (obj == null) throw new IllegalArgumentException("JSON is null");
        if (obj.has(FILTER_TAG_NAME)) {
            JSONObject filterDetails = obj.getJSONObject(FILTER_TAG_NAME);
            if (filterDetails.has(FILTER_TYPE_TAG_NAME)) {
                return new Pair<>(obj, filterDetails.getString(FILTER_TYPE_TAG_NAME));
            } else {
                throw new IllegalArgumentException("Invalid JSON (missing type)");
            }
        } else {
            throw new IllegalArgumentException("Invalid JSON (not a filter)");
        }
    }
    public static JSONObject getFilter(JSONObject obj, String type) {
        Pair<JSONObject, String> x = getFilter(obj);
        if (type.equals(x.second)) return x.first.getJSONObject(FILTER_TAG_NAME);
        else throw new IllegalArgumentException("Invalid JSON (wrong filter type)");
    }
}
