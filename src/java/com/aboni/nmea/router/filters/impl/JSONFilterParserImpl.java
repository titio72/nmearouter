package com.aboni.nmea.router.filters.impl;

import com.aboni.data.Pair;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.utils.ThingsFactory;
import org.json.JSONObject;

import javax.inject.Inject;

public class JSONFilterParserImpl implements JSONFilterParser {

    @Inject
    public JSONFilterParserImpl() {
        // do nothing
    }

    @Override
    public NMEAFilter getFilter(JSONObject obj) {
        Pair<JSONObject, String> x = JSONFilterUtils.getFilter(obj);
        obj = x.first;
        switch (x.second) {
            case DummyFilter.FILTER_TYPE:
                return DummyFilter.parseFilter(obj);
            case NMEABasicSentenceFilter.FILTER_TYPE:
                return NMEABasicSentenceFilter.parseFilter(obj);
            case N2KPGNFilter.FILTER_TYPE:
                return N2KPGNFilter.parseFilter(obj);
            case PositionFilter.FILTER_TYPE:
                return PositionFilter.parseFilter(obj);
            case NMEASpeedFilter.FILTER_TYPE:
                return NMEASpeedFilter.parseFilter(obj, ThingsFactory.getInstance(NMEACache.class));
            case NMEAFilterSetImpl.FILTER_TYPE:
                return NMEAFilterSetImpl.parseFilter(obj, this);
            default:
                throw new IllegalArgumentException("Unknown filter type");
        }
    }

}
