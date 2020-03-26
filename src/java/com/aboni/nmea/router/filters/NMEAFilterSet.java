package com.aboni.nmea.router.filters;

import com.aboni.nmea.sentences.NMEASentenceFilter;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.*;

public class NMEAFilterSet implements NMEASentenceFilterSet {

    private final List<NMEASentenceFilter> filters;
    private final Set<NMEASentenceFilter> filtersSet;

    private final TYPE whiteOrBlackList;

    public enum TYPE {
        BLACKLIST,
        WHITELIST
    }

    public NMEAFilterSet(TYPE type) {
        filters = new ArrayList<>();
        filtersSet = new HashSet<>();
        whiteOrBlackList = type;
    }
	
	public TYPE getType() {
        return whiteOrBlackList;
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

    public int count() {
        return filters.size();
    }

    public boolean match(Sentence sentence, String src) {
        if (!filters.isEmpty()) {
            for (Iterator<NMEASentenceFilter> i = getFilters(); i.hasNext(); ) {
                if (i.next().match(sentence, src)) return whiteOrBlackList == TYPE.WHITELIST;
            }
        }
        return whiteOrBlackList == TYPE.BLACKLIST;
    }
}
