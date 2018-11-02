package com.aboni.nmea.router.filters;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aboni.nmea.router.filters.NMEAFilterSet.TYPE;

import net.sf.marineapi.nmea.sentence.TalkerId;

public class FilterSetBuilder {

	public String exportFilter(NMEASentenceFilterSet s) {
		if (s!=null) {
			JSONObject obj = new JSONObject();
			JSONArray sentences = new JSONArray(); 
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
				} else if (f instanceof STalkFilter) {					
					JSONObject fJ = new JSONObject();
					if (((STalkFilter)f).isNegate()) {
						fJ.put("sentence", "STALK:!" + ((STalkFilter)f).getCOmmand());
					} else {
						fJ.put("sentence", "STALK:" + ((STalkFilter)f).getCOmmand());
					}
					sentences.put(fJ);
				}
			}
			obj.put("filters", sentences);
			if (s instanceof NMEAFilterSet) {
				obj.put("type", ((NMEAFilterSet)s).getType()==TYPE.BLACKLIST ? "blacklist":"whitelist");
			}
			return obj.toString();
		} else {
			return null;
		}
	}
	
	public NMEASentenceFilterSet importFilter(String s) {
		if (s!=null) {
			JSONObject jFs = null;
			try {
				jFs = new JSONObject(s);
			} catch (JSONException e) {
				
			}
			if (jFs!=null && jFs.has("filters")) {
				NMEAFilterSet res = new NMEAFilterSet();
				res.setType( ("whitelist".equals(jFs.getString("type"))) ?TYPE.WHITELIST:TYPE.BLACKLIST);
				JSONArray jFa = jFs.getJSONArray("filters");
				for (Object _fJ : jFa) {
					JSONObject fJ = (JSONObject) _fJ;
					NMEASentenceFilter f = null;
					String sentence = fJ.optString("sentence");
					if (sentence.startsWith("STALK:!")) {
						String cmd = sentence.substring("STALK:!".length());
						f = new STalkFilter(cmd, true);
					} else if (sentence.startsWith("STALK:")) {
							String cmd = sentence.substring("STALK:".length());
							f = new STalkFilter(cmd, false);
					} else {
						String stid =  fJ.optString("talker");
						if (stid==null || "".equals(stid)) {
							f = new NMEABasicSentenceFilter(fJ.getString("sentence"), fJ.getString("source"));
						} else {
							TalkerId tid = TalkerId.parse(stid);
							f = new NMEABasicSentenceFilter(fJ.getString("sentence"), tid, fJ.getString("source"));
						}
					}
					res.addFilter(f);
				}
				return res;
			}
		}
		return null;
	}
	
}
