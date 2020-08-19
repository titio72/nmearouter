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

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import java.lang.reflect.Constructor;
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

    public N2KMessageParserImpl() {
    }

    public N2KMessageParserImpl(String pgnString) throws PGNDataParseException {
        set(pgnString);
    }

    private void set(String pgnString) throws PGNDataParseException {
        pgnData = getDecodedHeader(pgnString);
        N2KMessageDefinitions.N2KDef d = N2KMessageDefinitions.getDefinition(pgnData.pgn);
        if (d != null && d.fast && pgnData.length == 8) {
            pgnData.expectedLength = getData()[1] & 0xFF;
            pgnData.currentFrame = getData()[0] & 0xFF;
            byte[] b = new byte[6];
            System.arraycopy(pgnData.data, 2, b, 0, 6);
            pgnData.data = b;
            pgnData.length = 6;
        }
        synchronized (STATS) {
            long l = STATS.getOrDefault((d != null) ? pgnData.getPgn() : -1, 0L);
            l++;
            STATS.put(pgnData.getPgn(), l);
        }
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
    public void addString(String s) throws PGNDataParseException {
        if (pgnData == null) {
            set(s);
        } else {
            PGNDecoded additionalPgn = getDecodedHeader(s);
            if (additionalPgn.pgn != pgnData.pgn) {
                throw new PGNDataParseException(String.format("Trying to add data to a different pgn: expected {%d} received {%d}", pgnData.pgn, additionalPgn.pgn));
            } else {
                int moreLen = additionalPgn.length;
                byte[] newData = new byte[pgnData.data.length + moreLen - 1];
                System.arraycopy(pgnData.data, 0, newData, 0, pgnData.data.length);
                int frameNo = additionalPgn.data[0] & 0x000000FF; // first byte is the n of the frame in the series
                if (frameNo != pgnData.currentFrame + 1) {
                    throw new PGNDataParseException(String.format("Trying to add non-consecutive frames to fast pgn:" +
                            " expected {%d} received {%d}", pgnData.currentFrame + 1, frameNo));
                } else {
                    pgnData.currentFrame = frameNo;
                    System.arraycopy(additionalPgn.data, 1, newData, pgnData.length, additionalPgn.data.length - 1);
                    pgnData.data = newData;
                    pgnData.length = newData.length;
                }
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
        return pgnData != null && N2KMessageDefinitions.isSupported(pgnData.pgn);
    }

    @Override
    public N2KMessage getMessage() throws PGNDataParseException {
        if (pgnData == null) return null;
        if (message == null) {
            N2KMessageDefinitions.N2KDef d = N2KMessageDefinitions.getDefinition(pgnData.pgn);
            if (d != null && d.constructor != null) {
                Constructor<?> constructor = d.constructor;
                try {
                    message = (N2KMessage) constructor.newInstance(pgnData, pgnData.data);
                } catch (Exception e) {
                    throw new PGNDataParseException("Error decoding N2K message", e);
                }
            } else {
                throw new PGNDataParseException("Unsupported PGN {" + pgnData.pgn + "}");
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
