package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.n2k.impl.*;

import java.lang.reflect.Constructor;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class N2KMessageParser {

    private static final Map<Integer, Class<? extends N2KMessage>> SUPPORTED = new HashMap<>();

    static {
        SUPPORTED.put(130306, N2KWindData.class); // Wind Data
        SUPPORTED.put(128267, N2KWaterDepth.class); // Water Depth
        SUPPORTED.put(128259, N2KSpeed.class); // Speed
        SUPPORTED.put(127250, N2KHeading.class); // Vessel Heading
        SUPPORTED.put(129025, N2KPositionRapid.class); // Position, Rapid update
        SUPPORTED.put(129026, N2KSOGAdCOGRapid.class); // COG & SOG, Rapid Update
        SUPPORTED.put(129029, N2KGNSSPositionUpdate.class); // GNSS Pos uptae
        SUPPORTED.put(126992, N2KSystemTime.class); // System time
        SUPPORTED.put(127257, N2KAttitude.class); // Attitude)
        SUPPORTED.put(130310, N2KEnvironment310.class); // Env parameter: Water temp, air temp, pressure
        SUPPORTED.put(130311, N2KEnvironment311.class); // Env parameter: temperature, humidity, pressure
        SUPPORTED.put(127245, N2KRudder.class); // Rudder
        SUPPORTED.put(127251, N2KRateOfTurn.class); // Rate of turn
        /*
        SUPPORTED.add(130577L); // Direction Data
        SUPPORTED.add(129291L); // Set & Drift, Rapid Update
        SUPPORTED.add(129809L); // AIS Class B static data (msg 24 Part A)
        SUPPORTED.add(129810L); // AIS Class B static data (msg 24 Part B)
        SUPPORTED.add(129039L); // AIS Class B position report
        SUPPORTED.add(129040L); // AIS Class B position report ext
        SUPPORTED.add(129794L); // AIS Class A Static and Voyage Related Data
        SUPPORTED.add(129038L); // AIS Class A Position Report
        SUPPORTED.add(127237L); // Heading track control
        SUPPORTED.add(65359L); // Seatalk: Pilot Heading
        SUPPORTED.add(65379L); // Seatalk: Pilot Mode
        SUPPORTED.add(65360L); // Seatalk: Pilot Locked Heading
        */
        /* to add */
        // "PGN": 129798, "Id": "aisSarAircraftPositionReport", "Description": "AIS SAR Aircraft Position Report"
        // "PGN": 65345,  "Id": "seatalkPilotWindDatum",        "Description": "Seatalk: Pilot Wind Datum"
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

    private PGNDecoded pgnData;
    private N2KMessage message;

    public N2KMessageParser(String pgnString) throws PGNDataParseException {
        parse(pgnString);
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
        return pgnData.expectedLength == 0 || pgnData.length < pgnData.expectedLength;
    }

    public void addMore(String s) throws PGNDataParseException {
        PGNDecoded additionalPgn = getDecodedHeader(s);
        if (additionalPgn.pgn != pgnData.pgn) {
            throw new PGNDataParseException(String.format("Trying to add data to a different pgn: expected {%d} received {%d}", pgnData.pgn, additionalPgn.pgn));
        } else {
            int moreLen = additionalPgn.length;
            byte[] newData = new byte[pgnData.data.length + moreLen - 1];
            System.arraycopy(pgnData.data, 0, newData, 0, pgnData.data.length); //skip the first 2
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

    private void parse(String s) throws PGNDataParseException {
        pgnData = getDecodedHeader(s);
    }

    public boolean isSupported() {
        return SUPPORTED.containsKey(pgnData.pgn);
    }

    public N2KMessage getMessage() throws PGNDataParseException {
        if (message == null) {
            Class<? extends N2KMessage> c = SUPPORTED.getOrDefault(pgnData.pgn, null);
            if (c != null) {
                Constructor<?> constructor = null;
                try {
                    constructor = c.getConstructor(new Class[]{N2KMessageHeader.class, (new byte[0]).getClass()});
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
        return Instant.parse(sTs);
    }
}
