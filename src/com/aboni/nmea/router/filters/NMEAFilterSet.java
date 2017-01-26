package com.aboni.nmea.router.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aboni.nmea.router.NMEASentenceFilterSet;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAFilterSet implements NMEASentenceFilterSet {
	
	private List<NMEASentenceFilter> filters;
	private Set<NMEASentenceFilter> filters_s;
	
	private boolean blackList;
	
	public enum TYPE {
		BLACKLIST,
		WHITELIST
	}
	
	public NMEAFilterSet(TYPE type) {
		filters = new ArrayList<NMEASentenceFilter>();
		filters_s = new HashSet<NMEASentenceFilter>();
		blackList = type==TYPE.BLACKLIST;
	}
	
	public NMEAFilterSet() {
		this(TYPE.BLACKLIST);
	}

	public void setType(TYPE t) {
		blackList = (t==TYPE.BLACKLIST);
	}

	public TYPE getType() {
		return (blackList?TYPE.BLACKLIST:TYPE.WHITELIST);
	}
	
	public void addFilter(NMEASentenceFilter f) {
		if (filters_s.contains(f)) {
			// do nothing... exists already
		} else {
			filters.add(f);
			filters_s.add(f);
		}
	}

	public void dropFilter(NMEASentenceFilter f) {
		if (filters_s.contains(f)) {
			filters.remove(f);
			filters_s.remove(f);
		} else {
			// do nothing... does not exist
		}		
	}
	
	public Iterator<NMEASentenceFilter> getFilters() {
		return filters.iterator();
	}
	
	public boolean match(Sentence sentence, String src) {
		if (filters.isEmpty()) {
			return blackList;
		} else {
			for (Iterator<NMEASentenceFilter> i = getFilters(); i.hasNext(); ) {
				if (i.next().match(sentence, src)) return !blackList;
			}
			return blackList;
		}
	}
}
