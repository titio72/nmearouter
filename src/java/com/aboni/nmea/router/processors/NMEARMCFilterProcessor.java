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

package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.filters.impl.NMEAPositionFilter;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEARMCFilterProcessor implements NMEAPostProcess {

    private final NMEAPositionFilter filter;
    private final NMEACache cache;
    private final RouterMessageFactory messageFactory;
    private final Log log;

    @Inject
    public NMEARMCFilterProcessor(@NotNull Log log, @NotNull NMEACache cache, @NotNull RouterMessageFactory messageFactory) {
        this.cache = cache;
        this.log = log;
        this.filter = new NMEAPositionFilter();
        this.messageFactory = messageFactory;
    }

    private static final Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            return (filter.match(messageFactory.createMessage(sentence, src, cache.getNow())) ? OK : KO);
        } catch (Exception e) {
            LogStringBuilder.start("RMCFilterProc").wO("process sentence").wV("sentence", sentence).error(log, e);
            return KO;
        }
    }

    private int i = 0;

    @Override
    public void onTimer() {
        i = (i+1) % 60;
        if (i==0) {
            filter.dumpStats(log);
        }
    }

}
