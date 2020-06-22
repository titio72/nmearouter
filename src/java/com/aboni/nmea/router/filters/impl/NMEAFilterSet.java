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

import com.aboni.nmea.router.filters.NMEASentenceFilterSet;
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
