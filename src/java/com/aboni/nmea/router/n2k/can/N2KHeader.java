package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.Instant;

public class N2KHeader implements N2KMessageHeader {

    private int pgn;
    private int priority;
    private int src;
    private int dst;

    public N2KHeader(long id) {
        getISO11783BitsFromCanId(id);
    }

    public int getPgn() {
        return pgn;
    }

    @Override
    public int getSource() {
        return src;
    }

    @Override
    public int getDest() {
        return dst;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public Instant getTimestamp() {
        return null;
    }

    private void getISO11783BitsFromCanId(long id) {
        int pf = (int) ((id >> 16) & 0xFF);
        int ps = (int) ((id >> 8) & 0xFF);
        int dp = (int) ((id >> 24) & 1);

        src = (int) (id & 0xFF);
        priority = (int) ((id >> 26) & 0x7);

        if (pf < 240) {
            /* PDU1 format, the PS contains the destination address */
            dst = ps;
            pgn = (dp << 16) + (pf << 8);
        } else {
            /* PDU2 format, the destination is implied global and the PGN is extended */
            dst = 0xff;
            pgn = (dp << 16) + (pf << 8) + ps;
        }
    }
}
