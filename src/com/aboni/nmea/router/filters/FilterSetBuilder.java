package com.aboni.nmea.router.filters;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

import net.sf.marineapi.nmea.sentence.TalkerId;

public class FilterSetBuilder {

	public String exportFilter(NMEASentenceFilterSet s) {
		JSONObject obj = new JSONObject();
		JSONArray sentences = new JSONArray(); 
		if (s!=null) {
			for (Iterator<NMEASentenceFilter> i = s.getFilters(); i.hasNext(); ) {
				NMEASentenceFilter f = i.next();
				if (f instanceof NMEABasicSentenceFilter) {
					JSONObject fJ = new JSONObject();
					if (((NMEABasicSentenceFilter) f).getTalkerId()==null) {
						fJ.put("talker", ((NMEABasicSentenceFilter)f).getTalkerId());
					} else {
						fJ.put("talker", ((NMEABasicSentenceFilter)f).getTalkerId().toString());
					}
					fJ.put("sentence", ((NMEABasicSentenceFilter)f).getSentenceId());
					fJ.put("source", ((NMEABasicSentenceFilter)f).getSource());
					sentences.put(fJ);
				}
			}
			obj.put("filters", sentences);
		}
		if (s instanceof NMEAFilterSet) {
			obj.put("type", ((NMEAFilterSet)s).getType()==TYPE.BLACKLIST ? "blacklist":"whitelist");
		}
		return obj.toString();
	}
	
	public NMEASentenceFilterSet importFilter(String s) {
		if (s!=null) {
			JSONObject jFs = new JSONObject(s);
			NMEAFilterSet res = new NMEAFilterSet();
			res.setType( ("whitelist".equals(jFs.getString("type"))) ?TYPE.WHITELIST:TYPE.BLACKLIST);
			JSONArray jFa = jFs.getJSONArray("filters");
			for (Object _fJ : jFa) {
				JSONObject fJ = (JSONObject) _fJ;
				NMEABasicSentenceFilter f = null;
				String stid =  fJ.optString("talker");
				if (stid==null || "".equals(stid)) {
					f = new NMEABasicSentenceFilter(fJ.getString("sentence"), fJ.getString("source"));
				} else {
					TalkerId tid = TalkerId.parse(stid);
					f = new NMEABasicSentenceFilter(fJ.getString("sentence"), tid, fJ.getString("source"));
				}
				res.addFilter(f);
			}
			return res;
		}
		return null;
	}
	
}
