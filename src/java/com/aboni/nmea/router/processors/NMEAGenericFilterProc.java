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

import com.aboni.nmea.sentences.NMEASentenceFilter;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEAGenericFilterProc implements NMEAPostProcess {

    private final NMEASentenceFilter filter;

    public NMEAGenericFilterProc(@NotNull NMEASentenceFilter filter) {
        this.filter = filter;
    }

    private static final Pair<Boolean, Sentence[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Sentence[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            return (filter.match(sentence, src) ? OK : KO);
        } catch (Exception e) {
            ServerLog.getLogger().warning("Filter processor: Cannot analyze sentence {" + sentence + "} error {" + e.getMessage() + "}");
            return KO;
        }
    }

    @Override
    public void onTimer() {
        // nothing to do on timer
    }
}
