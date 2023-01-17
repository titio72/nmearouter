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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class N2KMessageParserImpl implements N2KMessageParser {

    private static final Map<Integer, Long> STATS = new TreeMap<>();

    private static class PGNDecoded implements N2KMessageHeader {
        Instant ts;
        int pgn;
        int priority;
        int source;
        int dest;
        int length;
        int expectedLength;
        int currentFrame;
        byte[] data;

        @Override
        public int getPgn() {
            return pgn;
        }

        @Override
        public int getSource() {
            return source;
        }

        @Override
        public int getDest() {
            return dest;
        }

        @Override
        public int getPriority() {
            return priority;
        }

        @Override
        public Instant getTimestamp() {
            return ts;
        }
    }

    private PGNDecoded pgnData;
    private N2KMessage message;
    private boolean fast = false;
    private final N2KMessageFactory factory;

    @Inject
    public N2KMessageParserImpl(N2KMessageFactory factory) {
        if (factory==null) throw new IllegalArgumentException("N2K message factory is null");
        this.factory = factory;
    }

    private void set(PGNDecoded dec) throws PGNDataParseException {
        pgnData = dec;
        fast = factory.isFast(dec.pgn);
        boolean supported = factory.isSupported(dec.pgn);
        if (supported && fast && pgnData.length == 8) {
            pgnData.expectedLength = getData()[1] & 0xFF;
            pgnData.currentFrame = getData()[0] & 0xFF;
            if ((pgnData.currentFrame & 0x0F) != 0) {
                throw new PGNFastException("First frame of a sequence is expected to terminate with 0, received " + pgnData.currentFrame);
            }
            byte[] b = new byte[6];
            System.arraycopy(pgnData.data, 2, b, 0, 6);
            pgnData.data = b;
            pgnData.length = 6;
        }
        synchronized (STATS) {
            long l = STATS.getOrDefault(supported ? pgnData.getPgn() : -1, 0L);
            l++;
            STATS.put(pgnData.getPgn(), l);
        }
    }

    @Override
    public boolean isFast() {
        return fast;
    }

    @Override
    public int getFastSequenceNo() {
        return pgnData.currentFrame;
    }

    public static void resetStats() {
        synchronized (STATS) {
            STATS.clear();
        }
    }

    public static String dumpStats() {
        synchronized (STATS) {
            StringBuilder b = new StringBuilder();
            for (Map.Entry<Integer, Long> e : STATS.entrySet()) {
                b.append(e.getKey().toString()).append(",").append(e.getValue().toString()).append("\n");
            }
            return b.toString();
        }
    }

    @Override
    public N2KMessageHeader getHeader() {
        return pgnData;
    }

    @Override
    public int getLength() {
        return (pgnData == null) ? 0 : pgnData.length;
    }

    @Override
    public byte[] getData() {
        return (pgnData == null) ? null : pgnData.data;
    }

    @Override
    public boolean needMore() {
        return pgnData != null && (pgnData.length < pgnData.expectedLength);
    }

    @Override
    public void addMessage(N2KMessage msg) throws PGNDataParseException {
        PGNDecoded d = new PGNDecoded();
        d.data = msg.getData();
        d.dest = msg.getHeader().getDest();
        d.source = msg.getHeader().getSource();
        d.length = msg.getData().length;
        d.pgn = msg.getHeader().getPgn();
        d.priority = msg.getHeader().getPriority();
        d.ts = Instant.now();

        if (pgnData == null) {
            set(d);
        } else {
            add(d);
        }
    }

    @Override
    public void addString(String s) throws PGNDataParseException {
        PGNDecoded d = getDecodedHeader(s);
        if (pgnData == null) {
            set(d);
        } else {
            add(d);
        }
    }

    private void add(PGNDecoded additionalPgn) throws PGNDataParseException {
        if (additionalPgn.pgn != pgnData.pgn) {
            throw new PGNDataParseException(String.format("Trying to add data to a different pgn: expected {%d} received {%d}", pgnData.pgn, additionalPgn.pgn));
        } else {
            int moreLen = additionalPgn.length;
            byte[] newData = new byte[pgnData.data.length + moreLen - 1];
            System.arraycopy(pgnData.data, 0, newData, 0, pgnData.data.length);
            int frameNo = additionalPgn.data[0] & 0x000000FF; // first byte is the n of the frame in the series
            if (frameNo != pgnData.currentFrame + 1) {
                throw new PGNFastException(String.format("Trying to add non-consecutive frames to fast pgn:" +
                        " expected {%d} received {%d}", pgnData.currentFrame + 1, frameNo));
            } else {
                pgnData.currentFrame = frameNo;
                System.arraycopy(additionalPgn.data, 1, newData, pgnData.length, additionalPgn.data.length - 1);
                pgnData.data = newData;
                pgnData.length = newData.length;
            }
        }
    }

    private static PGNDecoded getDecodedHeader(String s) throws PGNDataParseException {
        try {
            StringTokenizer tok = new StringTokenizer(s.trim(), ",", false);
            PGNDecoded p = new PGNDecoded();
            p.ts = parseTimestamp(tok.nextToken());
            p.priority = Integer.parseInt(tok.nextToken());
            p.pgn = Integer.parseInt(tok.nextToken());
            p.source = Integer.parseInt(tok.nextToken());
            p.dest = Integer.parseInt(tok.nextToken());
            p.length = Integer.parseInt(tok.nextToken());
            p.data = new byte[p.length];
            for (int i = 0; i < p.length; i++) {
                String v = tok.nextToken();
                int c = Integer.parseInt(v, 16);
                p.data[i] = (byte) (c & 0xFF);
            }
            return p;
        } catch (Exception e) {
            throw new PGNDataParseException(String.format("Error parsing PGN data {%s}", s), e);
        }
    }

    @Override
    public boolean isSupported() {
        return pgnData != null && factory.isSupported(pgnData.pgn);
    }

    @Override
    public N2KMessage getMessage() throws PGNDataParseException {
        if (pgnData == null) return null;
        if (message == null) {
            try {
                message = factory.newInstance(pgnData, pgnData.data);
                if (message == null) {
                    message = factory.newUntypedInstance(pgnData, pgnData.data);
                }
            } catch (Exception e) {
                throw new PGNDataParseException("Error decoding N2K message", e);
            }
        }
        return message;
    }

    private static Instant parseTimestamp(String time) {
        //2011-11-24-22:42:04.437
        String sTs = time.substring(0, 10) + "T" + time.substring(11) + "Z";
        try {
            return Instant.parse(sTs);
        } catch (Exception e) {
            return Instant.now();
        }
    }
}
