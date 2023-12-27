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

package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.JSONFilterParser;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.utils.JSONUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class NMEAFilterSetImpl implements NMEAFilterSet {

    public static final String FILTER_TYPE = "set";
    public static final String WHITELIST = "whitelist";
    public static final String BLACKLIST = "blacklist";
    public static final String FILTERS = "filters";
    private final List<NMEAFilter> filterList;
    private final Set<NMEAFilter> filtersSet;

    private final TYPE whiteOrBlackList;

    public NMEAFilterSetImpl(TYPE type) {
        filterList = new ArrayList<>();
        filtersSet = new HashSet<>();
        whiteOrBlackList = type;
    }

    @Override
    public TYPE getType() {
        return whiteOrBlackList;
    }

    public void addFilter(NMEAFilter f) {
        if (!filtersSet.contains(f)) {
            filterList.add(f);
            filtersSet.add(f);
        }
    }

    public void dropFilter(NMEAFilter f) {
        if (filtersSet.contains(f)) {
            filterList.remove(f);
            filtersSet.remove(f);
        }
    }

    public Iterator<NMEAFilter> getFilters() {
        return filterList.iterator();
    }

    public int count() {
        return filterList.size();
    }

    @Override
    public boolean match(RouterMessage m) {
        if (!filterList.isEmpty()) {
            for (Iterator<NMEAFilter> i = getFilters(); i.hasNext(); ) {
                if (i.next().match(m)) return whiteOrBlackList == TYPE.WHITELIST;
            }
        }
        return whiteOrBlackList == TYPE.BLACKLIST;
    }

    @Override
    public boolean isEmpty() {
        return filterList.isEmpty();
    }

    @Override
    public JSONObject toJSON() {
        return JSONFilterUtils.createFilter(this, (JSONObject fltObj) -> {
            fltObj.put("logic", whiteOrBlackList == TYPE.BLACKLIST ? BLACKLIST : WHITELIST);
            JSONArray jFilters = new JSONArray();
            for (Iterator<NMEAFilter> i = getFilters(); i.hasNext(); ) {
                jFilters.put(i.next().toJSON());
            }
            fltObj.put(FILTERS, jFilters);
        });
    }

    public static NMEAFilterSet parseFilter(JSONObject obj, JSONFilterParser factory) {
        obj = JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        String logic = JSONUtils.getAttribute(obj, "logic", WHITELIST);
        TYPE tLogic;
        switch (logic) {
            case WHITELIST:
                tLogic = TYPE.WHITELIST;
                break;
            case BLACKLIST:
                tLogic = TYPE.BLACKLIST;
                break;
            default:
                throw new IllegalArgumentException("Unknown logic");
        }
        NMEAFilterSetImpl f = new NMEAFilterSetImpl(tLogic);
        if (obj.has(FILTERS)) {
            JSONArray fSet = obj.getJSONArray(FILTERS);
            for (int i = 0; i < fSet.length(); i++) {
                JSONObject fObj = fSet.getJSONObject(i);
                NMEAFilter flt = factory.getFilter(fObj);
                f.addFilter(flt);
            }
        }
        return f;
    }
}
