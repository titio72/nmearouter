package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.utils.JSONUtils;
import org.json.JSONObject;

public class N2KPGNFilter implements NMEAFilter {

    public static final String FILTER = "filter";

    private final int pgn;
    private final int src;
    private final int dst;
    private final String source;

    public N2KPGNFilter(int pgn, int src, int dst, String source) {
        this.pgn = pgn;
        this.dst = dst;
        this.src = src;
        this.source = source;
    }

    public N2KPGNFilter(int pgn, String source) {
        this.pgn = pgn;
        this.dst = 0xFF;
        this.src = 0xFF;
        this.source = source;
    }

    public N2KPGNFilter(int pgn) {
        this.pgn = pgn;
        this.dst = 0xFF;
        this.src = 0xFF;
        this.source = "";
    }

    public int getN2KSource() {
        return src;
    }

    public int getN2KDestination() {
        return dst;
    }

    public int getPgn() {
        return pgn;
    }

    public String getSource() {
        return source;
    }

    private boolean isAllPgn() {
        return pgn == -1;
    }

    public boolean isAllN2KSources() {
        return src == 0xFF;
    }

    public boolean isAllN2KDestinations() {
        return dst == 0xFF;
    }

    public boolean isAllSources() {
        return source.isEmpty();
    }

    @Override
    public boolean match(RouterMessage m) {
        if (m.getMessage() instanceof N2KMessage)
            return match((N2KMessage) m.getMessage(), m.getSource());
        else
            return true;
    }

    public boolean match(N2KMessage s, String src) {
        if (s != null) {
            return (isAllPgn() || getPgn() == s.getHeader().getPgn()) &&
                    (isAllN2KSources() || getN2KSource() == s.getHeader().getSource()) &&
                    (isAllN2KDestinations() || getN2KDestination() == s.getHeader().getDest()) &&
                    (isAllSources() || src.equals(getSource()));
        } else {
            return true;
        }
    }

    @Override
    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        JSONObject fltObj = new JSONObject();
        obj.put(FILTER, fltObj);
        fltObj.put("type", FILTER_TYPE);
        fltObj.put("pgn", pgn);
        fltObj.put("dst", dst);
        fltObj.put("src", src);
        fltObj.put("source", source);
        return obj;
    }

    public static N2KPGNFilter parseFilter(JSONObject obj) {
        obj = JSONFilterUtils.getFilter(obj, FILTER_TYPE);
        return new N2KPGNFilter(
                JSONUtils.getAttribute(obj, "pgn", 0x00),
                JSONUtils.getAttribute(obj, "dst", 0xFF),
                JSONUtils.getAttribute(obj, "src", 0xFF),
                JSONUtils.getAttribute(obj, "source", "")
        );
    }

    public static final String FILTER_TYPE = "pgn";
}
