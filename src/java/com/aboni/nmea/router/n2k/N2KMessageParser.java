package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.n2k.impl.*;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class N2KMessageParser {

    private static class N2KDef {
        Class<? extends N2KMessage> messageClass;
        boolean fast;

        static N2KDef getInstance(Class<? extends N2KMessage> c, boolean fast) {
            N2KDef d = new N2KDef();
            d.fast = fast;
            d.messageClass = c;
            return d;
        }
    }

    private static final Map<Integer, N2KDef> SUPPORTED = new HashMap<>();

    static {
        SUPPORTED.put(130306, N2KDef.getInstance(N2KWindData.class, false)); // Wind Data
        SUPPORTED.put(128267, N2KDef.getInstance(N2KWaterDepth.class, false)); // Water Depth
        SUPPORTED.put(128259, N2KDef.getInstance(N2KSpeed.class, false)); // Speed
        SUPPORTED.put(127250, N2KDef.getInstance(N2KHeading.class, false)); // Vessel Heading
        SUPPORTED.put(129025, N2KDef.getInstance(N2KPositionRapid.class, false)); // Position, Rapid update
        SUPPORTED.put(129026, N2KDef.getInstance(N2KSOGAdCOGRapid.class, false)); // COG & SOG, Rapid Update
        SUPPORTED.put(129029, N2KDef.getInstance(N2KGNSSPositionUpdate.class, true)); // GNSS Pos uptae
        SUPPORTED.put(129540, N2KDef.getInstance(N2KSatellites.class, true)); // List of sats
        SUPPORTED.put(126992, N2KDef.getInstance(N2KSystemTime.class, false)); // System time
        SUPPORTED.put(127257, N2KDef.getInstance(N2KAttitude.class, false)); // Attitude)
        SUPPORTED.put(130310, N2KDef.getInstance(N2KEnvironment310.class, false)); // Env parameter: Water temp, air temp, pressure
        SUPPORTED.put(130311, N2KDef.getInstance(N2KEnvironment311.class, false)); // Env parameter: temperature, humidity, pressure
        SUPPORTED.put(127245, N2KDef.getInstance(N2KRudder.class, false)); // Rudder
        SUPPORTED.put(127251, N2KDef.getInstance(N2KRateOfTurn.class, false)); // Rate of turn
        SUPPORTED.put(65359, N2KDef.getInstance(N2KSeatalkPilotHeading.class, false)); // Seatalk: Pilot Heading
        SUPPORTED.put(65360, N2KDef.getInstance(N2KSeatalkPilotLockedHeading.class, false)); // Seatalk: Pilot Locked Heading
        SUPPORTED.put(65379, N2KDef.getInstance(N2KSeatalkPilotMode.class, false)); // Seatalk: Pilot Mode
        SUPPORTED.put(65345, N2KDef.getInstance(N2KSeatalkPilotWindDatum.class, false)); // Seatalk: wind datum
        SUPPORTED.put(129038, N2KDef.getInstance(N2KAISPositionReportA.class, true)); // AIS position report class A
        SUPPORTED.put(129039, N2KDef.getInstance(N2KAISPositionReportB.class, true)); // AIS Class B position report
        SUPPORTED.put(129040, N2KDef.getInstance(N2KAISPositionReportBExt.class, true)); // AIS Class B position report
        SUPPORTED.put(129794, N2KDef.getInstance(N2KAISStaticDataA.class, true)); // AIS Class A Static and Voyage Related Data
        SUPPORTED.put(129809, N2KDef.getInstance(N2KAISStaticDataBPartA.class, true)); // AIS Class B static data (msg 24 Part A)
        SUPPORTED.put(129810, N2KDef.getInstance(N2KAISStaticDataBPartB.class, true)); // AIS Class B static data (msg 24 Part B)
        /*
        SUPPORTED.add(130577L); // Direction Data
        SUPPORTED.add(129291L); // Set & Drift, Rapid Update
        SUPPORTED.add(129040L); // AIS Class B position report ext
        */
        /* to add */
        // "PGN": 129798, "Id": "aisSarAircraftPositionReport", "Description": "AIS SAR Aircraft Position Report"
    }

    public static boolean isSupported(int pgn) {
        return SUPPORTED.containsKey(pgn);
    }

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

    private final PGNDecoded pgnData;
    private N2KMessage message;

    public N2KMessageParser(String pgnString) throws PGNDataParseException {
        pgnData = getDecodedHeader(pgnString);
        N2KDef d = SUPPORTED.getOrDefault(pgnData.pgn, null);
        if (d != null && d.fast && pgnData.length == 8) {
            pgnData.expectedLength = getData()[1] & 0xFF;
            pgnData.currentFrame = getData()[0] & 0xFF;
            byte[] b = new byte[6];
            System.arraycopy(pgnData.data, 2, b, 0, 6);
            pgnData.data = b;
            pgnData.length = 6;
        }
    }

    public N2KMessageHeader getHeader() {
        return pgnData;
    }

    public int getLength() {
        return pgnData.length;
    }

    public byte[] getData() {
        return pgnData.data;
    }

    public boolean needMore() {
        return pgnData.length < pgnData.expectedLength;
    }

    public void addMore(String s) throws PGNDataParseException {
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

    private PGNDecoded getDecodedHeader(String s) throws PGNDataParseException {
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

    public boolean isSupported() {
        return SUPPORTED.containsKey(pgnData.pgn);
    }

    public N2KMessage getMessage() throws PGNDataParseException {
        if (message == null) {
            N2KDef d = SUPPORTED.getOrDefault(pgnData.pgn, null);
            if (d != null) {
                Class<?> c = d.messageClass;
                Constructor<?> constructor;
                try {
                    constructor = c.getConstructor(N2KMessageHeader.class, byte[].class);
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
