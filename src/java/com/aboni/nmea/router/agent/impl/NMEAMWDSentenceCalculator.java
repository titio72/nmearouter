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

package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.NMEAMWDConverter;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEAMWDSentenceCalculator extends NMEAAgentImpl {

    private final NMEAMWDConverter converter;
    private long threshold;

    @Inject
    public NMEAMWDSentenceCalculator(@NotNull NMEACache cache) {
        super(cache);
        converter = new NMEAMWDConverter(TalkerId.II);
        setSourceTarget(true, true);
    }

    @Override
    protected final void onSetup(String name, QOS q) {
        if (q != null && q.get("longthreshold")) {
            threshold = 1000;
        } else {
            threshold = -1;
        }
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (s instanceof HDGSentence) {
            converter.setHeading((HDGSentence) s, getCache().getNow());
        } else if (s instanceof MWVSentence && ((MWVSentence) s).isTrue()) {
            converter.setWind((MWVSentence) s, getCache().getNow());
        } else {
            return;
        }
        MWDSentence outSentence = (threshold == -1) ? converter.getMWDSentence() : converter.getMWDSentence(threshold);
        if (outSentence != null) {
            notify(outSentence);
        }
    }

    @Override
    public String getDescription() {
        return "Calculate the absolute wind direction from the true wind and the heading.";
    }

    @Override
    public String getType() {
        return "MWD Calculator";
    }

    @Override
    public String toString() {
        return getType();
    }
}
