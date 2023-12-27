/*
 * Copyright (c) 2023,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.FilterFactory;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.filters.NMEAFilterSet;

public class FilterFactoryImpl implements FilterFactory {

    @Override
    public NMEAFilterSet createFilterSet(NMEAFilterSet.TYPE type) {
        if (type == null) throw new IllegalArgumentException("The type of the filter set cannot be null");
        return new NMEAFilterSetImpl(type);
    }

    @Override
    public NMEAFilter getNMEA0183Filter(String sentence) {
        return new NMEABasicSentenceFilter(sentence);
    }

    @Override
    public NMEAFilter getNMEA0183Filter(String sentence, String source) {
        return new NMEABasicSentenceFilter(sentence, source);
    }

    @Override
    public NMEAFilter getDummy(String data) {
        return new DummyFilter(data);
    }

    @Override
    public NMEAFilter getPositionFilter() {
        return new PositionFilter();
    }
}
