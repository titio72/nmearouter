package com.aboni.nmea.router.filters.impl;

import com.aboni.data.Pair;
import com.aboni.nmea.router.filters.NMEAFilter;
import org.json.JSONObject;

import java.lang.reflect.Field;

public class JSONFilterUtils {

    static final String FILTER_TAG_NAME = "filter";
    static final String FILTER_TYPE_TAG_NAME = "type";

    public interface JSONFilterFiller {
        void fillJSONFilter(JSONObject filterObject);
    }

    private JSONFilterUtils() {}

    public static JSONObject createFilter(NMEAFilter filter, JSONFilterFiller filler) {
        try {
            Field f = filter.getClass().getField("FILTER_TYPE");
            if (f.getType().equals(String.class)) {
                String type = (String) f.get(filter);
                JSONObject ret = new JSONObject();
                JSONObject flt = new JSONObject();
                flt.put(FILTER_TYPE_TAG_NAME, type);
                ret.put(FILTER_TAG_NAME, flt);
                if (filler != null) filler.fillJSONFilter(flt);
                return ret;
            } else {
                throw new IllegalArgumentException("Filter unknown - type is not recognized as string");
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("Filter unknown", e);
        }
    }

    /**
     * Extracts a JSON filter object from its parent.
     * Example:
     * {
     * filter: {
     * type: "dummy",
     * ...
     * }
     * }
     * Will return the JSON object tagged with "filter" paired with the string "dummy".
     *
     * @param obj The parent JSON object
     * @return A pair containing the filter JSON object and the type of the filter.
     */
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

    /**
     * Extracts a filter object from its parent JSON
     * Example:
     * {
     *     filter: {
     *         type: "dummy",
     *         ...
     *     }
     * }
     * The function will extract the JSON tagged with "filter".
     * The function will also check if the type of the filter is the one expected and throw an exception in case it does not match.
     * In the example, if the param "type" is not "dummy", the method will throw an IllegalArgumentException.
     * @param obj The parent JSON object
     * @param type The expected type of filter
     * @return The JSON filter object
     */
    public static JSONObject getFilter(JSONObject obj, String type) {
        Pair<JSONObject, String> x = getFilter(obj);
        if (type.equals(x.second)) return x.first.getJSONObject(FILTER_TAG_NAME);
        else throw new IllegalArgumentException("Invalid JSON (wrong filter type)");
    }
}
