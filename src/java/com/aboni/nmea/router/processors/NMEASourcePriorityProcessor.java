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
import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEASourcePriorityProcessor implements NMEAPostProcess {

    private static final long THRESHOLD = 30000L; // 0.5 minutes

    private final Map<String, Integer> priorities;
    private final Map<String, Long> lastSourceTimestamp;
    private final Set<String> sentences;

    private String currentSource;
    private int currentPriority;

    private final NMEACache cache;

    @Inject
    public NMEASourcePriorityProcessor(@NotNull NMEACache cache) {
        this.cache = cache;
        priorities = new HashMap<>();
        lastSourceTimestamp = new HashMap<>();
        sentences = Collections.synchronizedSet(new HashSet<>());
    }

    public void addAllGPS() {
        sentences.clear();
        sentences.addAll(Arrays.asList("RMC", "RMA", "RMB", "GLL", "VTG", "GGA", "GSA", "ZTG", "ZDA"));
    }

    public void addAllHeading() {
        sentences.clear();
        sentences.addAll(Arrays.asList("HDM", "HDT", "HDG"));
    }

    public void setSentences(String[] sentences) {
        this.sentences.addAll(Arrays.asList(sentences));
    }

    public void setPriority(String source, int priority) {
        priorities.put(source, priority);
    }

    private void recordInput(@NotNull Sentence sentence, @NotNull String source) {
        if (sentences.contains(sentence.getSentenceId())) {
            lastSourceTimestamp.put(source, cache.getNow());

            if (currentSource == null) {
                currentSource = source;
                currentPriority = priorities.getOrDefault(source, 0);
            } else {
                int priority = priorities.getOrDefault(source, 0);
                if (priority > currentPriority) {
                    // switch to higher priority source
                    currentSource = source;
                    currentPriority = priority;
                } else {
                    long now = cache.getNow();
                    long currentLastSourceTimestamp = lastSourceTimestamp.getOrDefault(currentSource, 0L);
                    if ((now - currentLastSourceTimestamp) > THRESHOLD) {
                        // switch to a lower priority source because the higher priority has not been available for a while
                        currentSource = source;
                        currentPriority = priority;
                    }
                }
            }
        }
    }

    private static final Sentence[] EMPTY = new Sentence[] {};

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        if (sentences.contains(sentence.getSentenceId())) {
            recordInput(sentence, src);
            if (src.equals(currentSource)) {
                return new Pair<>(true, EMPTY);
            } else {
                // force skip the sentence
                return new Pair<>(false, EMPTY);
            }
        } else {
            return new Pair<>(true, EMPTY);
        }

    }

    @Override
    public void onTimer() {
        // nothing to do on timer
    }
}
