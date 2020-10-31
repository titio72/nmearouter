package com.aboni.nmea.router.n2k.evo;

import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.Instant;

class N2kHeader126208 implements N2KMessageHeader {

    private final int src;
    private final Instant t;

    N2kHeader126208(int src, Instant time) {
        this.src = src;
        this.t = time;
    }

    @Override
    public int getPgn() {
        return 126208;
    }

    @Override
    public int getSource() {
        return src;
    }

    @Override
    public int getDest() {
        return 204;
    }

    @Override
    public int getPriority() {
        return 3;
    }

    @Override
    public Instant getTimestamp() {
        return t;
    }
}
