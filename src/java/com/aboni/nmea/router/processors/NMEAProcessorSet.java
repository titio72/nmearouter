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

import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMEAProcessorSet {

    private final List<NMEAPostProcess> processors;
    private static final List<Sentence> EMPTY = new ArrayList<>();

    public NMEAProcessorSet() {
        processors = new ArrayList<>();
    }

    public List<Sentence> getSentences(Sentence sentence, String source) throws NMEARouterProcessorException {
        List<Sentence> toSend = new ArrayList<>();
        toSend.add(sentence);
        synchronized (processors) {
            for (NMEAPostProcess pp : processors) {
                Pair<Boolean, Sentence[]> res = pp.process(sentence, source);
                if (res != null) {
                    if (!Boolean.TRUE.equals(res.first)) {
                        return EMPTY;
                    } else if (res.second != null) {
                        Collections.addAll(toSend, res.second);
                    }
                }
            }
        }
        return toSend;
    }

    /**
     * Sources can use post-process delegates to add additional elaboration to the sentences they pushes into the stream.
     *
     * @param f The post processor to be added (sequence is important)
     */
    public final void addProcessor(NMEAPostProcess f) {
        synchronized (processors) {
            processors.add(f);
        }
    }

    public final void onTimer() {
        synchronized (processors) {
            for (NMEAPostProcess p : processors) {
                p.onTimer();
            }
        }
    }

}
