package com.aboni.nmea.router.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aboni.nmea.router.Filterable;

import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAFilterSet implements Filterable {
	
	private List<NMEASentenceFilter> filters;
	private Set<NMEASentenceFilter> filters_s;
	
	public NMEAFilterSet() {
		filters = new ArrayList<NMEASentenceFilter>();
		filters_s = new HashSet<NMEASentenceFilter>();
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
	
	public boolean accept(Sentence sentence, String src) {
		if (filters.isEmpty()) {
			return true;
		} else {
			for (Iterator<NMEASentenceFilter> i = getFilters(); i.hasNext(); ) {
				NMEASentenceFilter.FILTERACTION r = i.next().accept(sentence, src);
				if (r==NMEASentenceFilter.FILTERACTION.REJECT) {
					return false;
				}
			}
			return true;
		}
	}
}
