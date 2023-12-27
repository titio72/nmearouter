package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.n2k.N2KMessage;
import com.aboni.nmea.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.utils.JSONUtils;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.json.JSONObject;

public class NMEABasicSentenceFilter implements NMEAFilter {

    public static final String FILTER_TYPE = "nmea";
    public static final String NMEA_TALKER_JSON_TAG = "talker";
    public static final String NMEA_SENTENCE_JSON_TAG = "sentence";
    public static final String NMEA_SOURCE_JSON_TAG = "source";
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

    public static NMEABasicSentenceFilter parseFilter(JSONObject obj) {
        obj = JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        return new NMEABasicSentenceFilter(
                JSONUtils.getAttribute(obj, NMEA_SENTENCE_JSON_TAG, ""),
                obj.has(NMEA_TALKER_JSON_TAG) ?
                        TalkerId.parse(obj.getString(NMEA_TALKER_JSON_TAG)) : null,
                JSONUtils.getAttribute(obj, NMEA_SOURCE_JSON_TAG, "")
        );
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

    public boolean isAllSources() {
        return source.isEmpty();
    }

    @Override
    public boolean match(RouterMessage m) {
        if (m.getPayload() instanceof N2KMessage) {
            return matchN2K((N2KMessage) m.getPayload(), m.getAgentSource());
        } else if (m.getPayload() instanceof NMEA0183Message) {
            return match(((NMEA0183Message) m.getPayload()).getSentence(), m.getAgentSource());
        } else {
            return true;
        }
    }

    private boolean matchN2K(N2KMessage n2KMessage, String source) {
        if (n2KMessage != null) {
            if (pgn == -1) {
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

    @Override
    public JSONObject toJSON() {
        return JSONFilterUtils.createFilter(this, (JSONObject fltObj) -> {
            if (talkerId != null) fltObj.put(NMEA_TALKER_JSON_TAG, talkerId.toString());
            fltObj.put(NMEA_SENTENCE_JSON_TAG, sentenceId);
            fltObj.put(NMEA_SOURCE_JSON_TAG, source);
        });
    }
}
