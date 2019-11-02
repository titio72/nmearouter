package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMEAProcessorSet {

    private final List<NMEAPostProcess> proc;
    private static final List<Sentence> EMPTY = new ArrayList<>();

    public NMEAProcessorSet() {
        proc = new ArrayList<>();
    }

    public List<Sentence> getSentences(Sentence sentence, String source) {
        List<Sentence> toSend = new ArrayList<>();
        toSend.add(sentence);
        synchronized (proc) {
            for (NMEAPostProcess pp : proc) {
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
     * Sources can use post-proc delegates to add additional elaboration to the sentences they pushes into the stream.
     * @param f The post processor to be added (sequence is important)
     */
    public final void addProcessor(NMEAPostProcess f) {
        synchronized (proc) {
            proc.add(f);
        }
    }

    public final void onTimer() {
        synchronized (proc) {
            for (NMEAPostProcess p: proc) {
                p.onTimer();
            }
        }
    }

}
