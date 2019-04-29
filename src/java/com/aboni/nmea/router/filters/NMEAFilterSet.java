package com.aboni.nmea.router.filters;

import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.*;

public class NMEAFilterSet implements NMEASentenceFilterSet {
	
	private final List<NMEASentenceFilter> filters;
	private final Set<NMEASentenceFilter> filtersSet;
	
	private boolean blackList;
	
	public enum TYPE {
		BLACKLIST,
		WHITELIST
	}
	
	public NMEAFilterSet(TYPE type) {
		filters = new ArrayList<>();
		filtersSet = new HashSet<>();
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
		if (!filtersSet.contains(f)) {
			filters.add(f);
			filtersSet.add(f);
		}
	}

	public void dropFilter(NMEASentenceFilter f) {
		if (filtersSet.contains(f)) {
			filters.remove(f);
			filtersSet.remove(f);
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
