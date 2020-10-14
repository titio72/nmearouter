package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.n2k.N2KMessage;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;

public class NMEABasicSentenceFilter implements NMEAFilter {

    private final String sentenceId;
    private final TalkerId talkerId;
    private final String source;
    private int pgn = -1;
    private int n2kSource = -1;

    public NMEABasicSentenceFilter(String sentenceId, TalkerId talkerId, String source) {
        if (sentenceId == null) sentenceId = "";
        this.sentenceId = sentenceId;
        this.talkerId = talkerId;
        this.source = source;
    }

    public NMEABasicSentenceFilter(String sentenceId, String source) {
        if (sentenceId == null) sentenceId = "";
        this.sentenceId = sentenceId;
        this.talkerId = null;
        this.source = source;
    }

    public NMEABasicSentenceFilter(String sentenceId) {
        if (sentenceId == null) sentenceId = "";
        this.sentenceId = sentenceId;
        this.talkerId = null;
        this.source = "";
    }

    public TalkerId getTalkerId() {
        return talkerId;
    }

    public String getSentenceId() {
        return sentenceId;
    }

    public String getSource() {
        return source;
    }

    private boolean isAllSentences() {
        return sentenceId.isEmpty();
    }

    private boolean isAllTalkers() {
        return talkerId == null;
    }

    private boolean isAllSources() {
        return source.isEmpty();
    }

    @Override
    public boolean match(RouterMessage m) {
        if (m.getPayload() instanceof N2KMessage) {
            return matchN2K((N2KMessage) m.getMessage(), m.getSource());
        } else {
            return match(m.getSentence(), m.getSource());
        }
    }

    private boolean matchN2K(N2KMessage n2KMessage, String source) {
        if (n2KMessage!=null) {
            if (pgn==-1) {
                try {
                    String[] ss = sentenceId.split(":");
                    pgn = Integer.parseInt(ss[0]);
                    n2kSource = (ss.length > 1) ? Integer.parseInt(ss[1]) : 0xFF;
                } catch (Exception ignored) {
                    // do nothing with it
                    pgn = 0;
                    n2kSource = 0xFF;
                }
            }
            return ((isAllSources() || getSource().equals(source)) &&
                    (pgn == 0 || pgn == n2KMessage.getHeader().getPgn()) &&
                    (n2kSource == 0xFF || n2kSource == n2KMessage.getHeader().getSource()));
        } else {
            return true;
        }
    }

    public boolean match(Sentence s, String src) {
        if (s != null) {
            if ((isAllSources() || getSource().equals(src)) && (isAllSentences() || getSentenceId().equals(s.getSentenceId()))) {
                return isAllTalkers() || getTalkerId().equals(s.getTalkerId());
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
