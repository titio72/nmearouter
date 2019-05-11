package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEASourcePriorityProcessor implements NMEAPostProcess {

    private static final long THRESHOLD = 120000L; // 2 minutes

    private Map<String, Integer> priorities;
    private Map<String, Long> lastSourceTimestamp;

    private Set<String> sentences;

    private String currentSource;
    private int currentPriority;

    long timeStamp = -1;

    private long getNow() {
        return timeStamp==-1 ? System.currentTimeMillis() : timeStamp;
    }

    public NMEASourcePriorityProcessor() {
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
            lastSourceTimestamp.put(source, getNow());

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
                    long now = getNow();
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
