package com.aboni.nmea.router.filters.impl;

import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.n2k.N2KMessage;

public class N2KPGNFilter implements NMEAFilter {

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
        return match(m.getN2KMessage(), m.getSource());
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
}
