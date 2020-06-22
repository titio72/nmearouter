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

import com.aboni.nmea.router.filters.FilterSetSerializer;
import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
import com.aboni.nmea.router.filters.impl.NMEAFilterSet.TYPE;
import com.aboni.nmea.sentences.NMEABasicSentenceFilter;
import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class JSONFilterSetSerializer implements FilterSetSerializer {

    private static final String SENTENCE = "sentence";
    private static final String TALKER = "talker";
    private static final String SOURCE = "source";
    private static final String STALK_NEGATE = "STALK:!";
    private static final String STALK = "STALK:";
    private static final String FILTERS = "filters";

    @Override
    public String exportFilter(NMEASentenceFilterSet s) {
        if (s != null) {
            JSONObject obj = new JSONObject();
            JSONArray sentences = new JSONArray();
            for (Iterator<NMEASentenceFilter> i = s.getFilters(); i.hasNext(); ) {
                NMEASentenceFilter f = i.next();
                if (f instanceof NMEABasicSentenceFilter) {
                    sentences.put(getJSONFilter((NMEABasicSentenceFilter) f));
                } else if (f instanceof STalkFilter) {
                    sentences.put(getJSONFilter((STalkFilter) f));
                }
            }
            obj.put(FILTERS, sentences);
            if (s instanceof NMEAFilterSet) {
                obj.put("type", ((NMEAFilterSet) s).getType() == TYPE.BLACKLIST ? "blacklist" : "whitelist");
            }
            return obj.toString();
        } else return null;
    }

    @Override
    public NMEASentenceFilterSet importFilter(String jsonFilter) {
        if (jsonFilter != null && !jsonFilter.isEmpty()) {
            JSONObject jFs = new JSONObject(jsonFilter);
            if (jFs.has(FILTERS)) {
                NMEAFilterSet res = new NMEAFilterSet(("whitelist".equals(jFs.getString("type"))) ? TYPE.WHITELIST : TYPE.BLACKLIST);
                JSONArray jFa = jFs.getJSONArray(FILTERS);
                for (Object _fJ : jFa) {
                    NMEASentenceFilter f = getNMEASentenceFilter((JSONObject) _fJ);
                    res.addFilter(f);
                }
                return res;
            }
        }
        return null;
    }

    private JSONObject getJSONFilter(STalkFilter f) {
        JSONObject fJ = new JSONObject();
        if (f.isNegate()) {
            fJ.put(SENTENCE, STALK_NEGATE + f.getCommand());
        } else {
            fJ.put(SENTENCE, STALK + f.getCommand());
        }
        return fJ;
    }

    private JSONObject getJSONFilter(NMEABasicSentenceFilter f) {
        JSONObject fJ = new JSONObject();
        if (f.getTalkerId()==null) {
            fJ.put(TALKER, f.getTalkerId());
        } else {
            fJ.put(TALKER, f.getTalkerId().toString());
        }
        fJ.put(SENTENCE, f.getSentenceId());
        fJ.put(SOURCE, f.getSource());
        return fJ;
    }

    private NMEASentenceFilter getNMEASentenceFilter(JSONObject filter) {
        NMEASentenceFilter f;
        String sentence = filter.optString(SENTENCE);
        if (sentence.startsWith(STALK_NEGATE)) {
            String cmd = sentence.substring(STALK_NEGATE.length());
            f = new STalkFilter(cmd, true);
        } else if (sentence.startsWith(STALK)) {
            String cmd = sentence.substring(STALK.length());
            f = new STalkFilter(cmd, false);
        } else {
            String id = filter.optString(TALKER);
            if (id == null || "".equals(id)) {
                f = new NMEABasicSentenceFilter(filter.getString(SENTENCE), filter.getString(SOURCE));
            } else {
                TalkerId tid = TalkerId.parse(id);
                f = new NMEABasicSentenceFilter(filter.getString(SENTENCE), tid, filter.getString(SOURCE));
            }
        }
        return f;
    }

}
