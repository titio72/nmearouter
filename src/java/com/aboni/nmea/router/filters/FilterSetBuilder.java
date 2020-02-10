package com.aboni.nmea.router.filters;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;
import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class FilterSetBuilder {

	private static final String SENTENCE = "sentence";
	private static final String TALKER = "talker";
	private static final String SOURCE = "source";
	private static final String STALK_NEGATE = "STALK:!";
	private static final String STALK = "STALK:";
	private static final String FILTERS = "filters";

	public String exportFilter(NMEASentenceFilterSet s) {
		if (s!=null) {
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
				obj.put("type", ((NMEAFilterSet)s).getType()==TYPE.BLACKLIST ? "blacklist":"whitelist");
			}
			return obj.toString();
		} else return null;
	}

	private JSONObject getJSONFilter(STalkFilter f) {
		JSONObject fJ = new JSONObject();
		if (f.isNegate()) {
			fJ.put(SENTENCE, STALK_NEGATE + f.getCOmmand());
		} else {
			fJ.put(SENTENCE, STALK + f.getCOmmand());
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

	public NMEASentenceFilterSet importFilter(String jsonFilter) {
		if (jsonFilter!=null && !jsonFilter.isEmpty()) {
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
			String stid =  filter.optString(TALKER);
			if (stid==null || "".equals(stid)) {
				f = new NMEABasicSentenceFilter(filter.getString(SENTENCE), filter.getString(SOURCE));
			} else {
				TalkerId tid = TalkerId.parse(stid);
				f = new NMEABasicSentenceFilter(filter.getString(SENTENCE), tid, filter.getString(SOURCE));
			}
		}
		return f;
	}

}
