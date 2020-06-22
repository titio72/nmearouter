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

import com.aboni.utils.HWSettings;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;

/**
 * Enrich HDG heading information:
 * 1) Listen to GPS location to set the magnetic variation into the HDG sentence (if not present)
 * 2) Split the sentence in HDM & HDT   
 * @author aboni
 *
 */
public class NMEADepthEnricher implements NMEAPostProcess {

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        try {
            if (sentence instanceof DBTSentence) {
                double offset = HWSettings.getPropertyAsDouble("depth.offset", 0.0);
                DBTSentence dbt = (DBTSentence)sentence;
                DPTSentence dpt = (DPTSentence)SentenceFactory.getInstance().createParser(dbt.getTalkerId(), SentenceId.DPT);
                dpt.setDepth(dbt.getDepth());
                dpt.setOffset(offset);
                return new Pair<>(Boolean.TRUE, new Sentence[] {dpt});
            }
        } catch (Exception e) {
            ServerLog.getLogger().warning("Cannot enrich depth process message {" + sentence + "} error {" + e.getLocalizedMessage() + "}");
        }
        return new Pair<>(Boolean.TRUE, null);
    }

    @Override
    public void onTimer() {
        // nothing to do
    }
}
