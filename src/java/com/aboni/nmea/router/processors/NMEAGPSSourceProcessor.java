package com.aboni.nmea.router.processors;

import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NMEAGPSSourceProcessor implements NMEAPostProcess {

    private static final long THRESHOLD = 120000L; // 2 minutes

    private Map<String, Integer> priorities;
    private Map<String, Long> lastSourceTimestamp;

    private Set<String> sentences;

    private String currentSource;
    private int currentPriority;

    public NMEAGPSSourceProcessor() {
        priorities = new HashMap<>();
        lastSourceTimestamp = new HashMap<>();
        sentences = new HashSet<>();
        sentences.add("RMC");
        sentences.add("RMA");
        sentences.add("RMB");
        sentences.add("GLL");
        sentences.add("VTG");
        sentences.add("GGA");
        sentences.add("GSA");
        sentences.add("ZTG");
        sentences.add("ZDA");
    }

    public void setPriority(String source, int priority) {
        priorities.put(source, priority);
    }

    private void recordInput(@NotNull Sentence sentence, @NotNull String source) {
        if (sentences.contains(sentence.getSentenceId())) {
            lastSourceTimestamp.put(source, System.currentTimeMillis());

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
                    long now = System.currentTimeMillis();
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

    @Override
    public Pair<Boolean, Sentence[]> process(Sentence sentence, String src) {
        recordInput(sentence, src);
        return null;
    }

    @Override
    public void onTimer() {
        // nothing to do on timer
    }
}
