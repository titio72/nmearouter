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

package com.aboni.nmea.router.n2k;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.impl.BitUtils;
import com.aboni.nmea.router.n2k.impl.N2KLookupTables;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public class PGNParser {

    private static final Set<Long> SUPPORTED = new HashSet<>();
    private static boolean experimental = false;

    static {
        SUPPORTED.add(130306L); // Wind Data
        SUPPORTED.add(127245L); // Rudder
        SUPPORTED.add(127250L); // Vessel Heading
        SUPPORTED.add(127251L); // Rate of turn
        SUPPORTED.add(127257L); // Attitude
        SUPPORTED.add(129025L); // Position, Rapid update
        SUPPORTED.add(129026L); // COG & SOG, Rapid Update
        SUPPORTED.add(130310L); // Environmental Parameters (water temp)
        SUPPORTED.add(129033L); // Time & Date
        SUPPORTED.add(126992L); // System time
        SUPPORTED.add(129291L); // Set & Drift, Rapid Update
        SUPPORTED.add(128267L); // Water Depth
        SUPPORTED.add(128259L); // Speed
        SUPPORTED.add(130312L); // Temperature (does not work)
        SUPPORTED.add(130311L); // Temperature (does not work)
        SUPPORTED.add(129809L); // AIS Class B static data (msg 24 Part A)
        SUPPORTED.add(129810L); // AIS Class B static data (msg 24 Part B)
        SUPPORTED.add(129039L); // AIS Class B position report
        SUPPORTED.add(129040L); // AIS Class B position report ext
        SUPPORTED.add(129794L); // AIS Class A Static and Voyage Related Data
        SUPPORTED.add(129038L); // AIS Class A Position Report
        SUPPORTED.add(127237L); // Heading track control
        SUPPORTED.add(130577L); // Direction Data
        SUPPORTED.add(65359L); // Seatalk: Pilot Heading
        SUPPORTED.add(65379L); // Seatalk: Pilot Mode
        SUPPORTED.add(65360L); // Seatalk: Pilot Locked Heading
        /* to add */
        // "PGN": 129798, "Id": "aisSarAircraftPositionReport", "Description": "AIS SAR Aircraft Position Report"
        // "PGN": 65345,  "Id": "seatalkPilotWindDatum",        "Description": "Seatalk: Pilot Wind Datum"
    }

    private static final DateTimeFormatter tsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));

    public static class PGNDataParseException extends Exception {
        PGNDataParseException(long pgn) {
            super(String.format("PGN %d is unsupported", pgn));
        }

        PGNDataParseException(String msg) {
            super(msg);
        }

        PGNDataParseException(String msg, Throwable t) {
            super(msg, t);
        }
    }

    public static void setExperimental() {
        experimental = true;
    }

    public static boolean isSupported(long pgn) {
        return SUPPORTED.contains(pgn);
    }

    static class PGNDecoded {
        Instant ts;
        int pgn;
        int priority;
        int source;
        int dest;
        int length;
        int expectedLength;
        int currentFrame;
        byte[] data;
        JSONObject canBoatJson;
    }

    private PGNDecoded pgnData;
    private PGNDef definition;
    private boolean debug;

    public PGNParser(@NotNull PGNs pgnDefinitions, String pgnString) throws PGNDataParseException {
        parse(pgnDefinitions, pgnString);
    }

    public void setDebug() {
        debug = true;
    }

    public Instant getTime() {
        return pgnData.ts;
    }

    public int getPgn() {
        return pgnData.pgn;
    }

    public int getPriority() {
        return pgnData.priority;
    }

    public int getSource() {
        return pgnData.source;
    }

    public int getDest() {
        return pgnData.dest;
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

    public JSONObject getCanBoatJson() throws PGNDataParseException {
        if (definition == null) throw new PGNDataParseException(pgnData.pgn);
        if (pgnData.canBoatJson == null /*&& !needMore()*/ && (experimental || isSupported(pgnData.pgn))) {
            JSONObject res = new JSONObject();
            res.put("pgn", pgnData.pgn);
            res.put("prio", pgnData.priority);
            res.put("src", pgnData.source);
            res.put("dst", pgnData.dest);
            res.put("timestamp", tsFormatter.format(pgnData.ts));
            res.put("description", definition.getDescription());
            PGNDef.PGNFieldDef[] fieldDefinitions = definition.getFields();
            if (fieldDefinitions != null) {
                JSONObject jF = parse(fieldDefinitions, pgnData.data);
                res.put("fields", jF);
            }
            pgnData.canBoatJson = res;
        }
        return pgnData.canBoatJson;
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
            StringTokenizer tok = new StringTokenizer(s, ",", false);
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

    private void parse(PGNs pgnDefinitions, String s) throws PGNDataParseException {
        pgnData = getDecodedHeader(s);

        PGNDef def = pgnDefinitions.getPGN(pgnData.pgn);
        if (def != null) {
            definition = def;
            if (def.getType() == PGNDef.PGNType.FAST && pgnData.length == 8 /* if the length is >8 we can assume the pgn has been "extended" already */) {
                pgnData.currentFrame = getData()[0] & 0x000000FF;
                pgnData.expectedLength = getData()[1] & 0x000000FF;
                byte[] newData = new byte[pgnData.length - 2];
                System.arraycopy(pgnData.data, 2, newData, 0, pgnData.data.length - 2);
                pgnData.data = newData;
                pgnData.length = pgnData.data.length;
            }
        }
    }

    private Instant parseTimestamp(String time) {
        //2011-11-24-22:42:04.437
        String sTs = time.substring(0, 10) + "T" + time.substring(11) + "Z";
        return Instant.parse(sTs);
    }

    private JSONObject parse(PGNDef.PGNFieldDef[] fields, byte[] data) {
        JSONObject json = new JSONObject();
        for (PGNDef.PGNFieldDef def : fields) {
            if (debug) ServerLog.getConsoleOut().print("Parsing " + def.getName() + " [" + def.getType() + "]");
            String res = null;
            if (!"reserved".equals(def.getId())) {
                switch (def.getType()) {
                    case "Lookup table":
                        res = parseEnum(data, json, def);
                        break;
                    case "ASCII text":
                        res = parseAscii(data, json, def);
                        break;
                    default:
                        res = parseValue(data, json, def);
                }
            }
            if (debug) ServerLog.getConsoleOut().println("[" + res + "]");
        }
        return json;
    }

    private static String parseAscii(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();

        int totBits = data.length * 8;
        if (offset < totBits && (offset + length) > totBits && (totBits - offset) >= 8) length = totBits - offset;
        if (offset + length > totBits) return null;


        byte[] b = new byte[(int) Math.ceil(length / 8.0)];
        for (int i = 0; i < length; i += 8) {
            long e = BitUtils.extractBits(data, start, offset + i, 8, false).v;
            b[i / 8] = (byte) e;
        }
        String res = getASCII(b);
        json.put(def.getName(), res);
        return res;
    }

    private static String parseEnum(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, def.isSigned());
        int reserved = 0;
        if (e.m >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }

        String ret = null;
        if (e.v <= (e.m - reserved)) {
            String enumDesc = def.getENumValue((int) e.v);
            if (enumDesc != null) {
                json.put(def.getName(), enumDesc);
                ret = enumDesc;
            } else {
                json.put(def.getName(), "" + e.v);
                ret = "" + e.v;
            }
        }
        return ret;
    }

    private static String parseValue(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, def.isSigned());
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }

        return handleValue(json, def, e, reserved);
    }

    private static String handleValue(JSONObject json, PGNDef.PGNFieldDef def, BitUtils.Res e, int reserved) {
        if (e.v <= e.m - reserved) {
            double vv = e.v / ((int) 1.0 / def.getResolution());
            if ("Manufacturer code".equalsIgnoreCase(def.getType())) {
                String s = N2KLookupTables.getTable("manufacturerCode").getOrDefault((int) e.v, String.format("%d", e.v));
                json.put(def.getName(), s);
                return s;
            } else if ("Pressure".equals(def.getType())) {
                vv = e.v * 100.0; // put it back into Pa instead of HPa
                json.put(def.getName(), vv);
                return "" + vv;
            } else if ("rad".equals(def.getUnits())) {
                vv = Utils.round(Math.toDegrees(vv), 1);
                json.put(def.getName(), vv);
                return "" + vv;
            } else if ("rad/s".equals(def.getUnits())) {
                vv = Utils.round(Math.toDegrees(vv), 5);
                json.put(def.getName(), vv);
                return "" + vv;
            } else if ("days".equals(def.getUnits())) {
                long l = e.v * 24 * 60 * 60 * 1000;
                OffsetDateTime dt = OffsetDateTime.ofInstant(Instant.ofEpochMilli(l), ZoneId.of("UTC"));
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
                String dateString = fmt.withZone(ZoneId.of("UTC")).format(dt);
                json.put(def.getName(), dateString);
                return dateString;
            } else if ("s".equals(def.getUnits())) {
                long vvv = (long) vv;
                String timeString = String.format("%02d:%02d:%02d", vvv / 3600, (vvv % 3600) / 60, vvv % 60);
                json.put(def.getName(), timeString);
                return timeString;
            } else if ("K".equals(def.getUnits())) {
                // transform Kelvin to Celsius
                vv = Utils.round(vv - 273.15, 1);
                json.put(def.getName(), vv);
                return "" + vv;
            } else {
                if (def.getResolution() == 1) {
                    json.put(def.getName(), e.v);
                    return "" + e.v;
                } else {
                    json.put(def.getName(), vv);
                    return "" + vv;
                }
            }
        }
        return null;
    }

    private static String getASCII(byte[] b) {
        // remove padding
        int l = b.length;
        char last = (char) b[l - 1];
        while (last == 0xff || last == ' ' || last == 0 || last == '@') {
            l--;
            last = (char) b[l - 1];
        }
        char c;
        int k;

        StringBuilder sb = new StringBuilder(l);
        // escape
        for (k = 0; k < l; k++) {
            c = (char) b[k];
            switch (c) {
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;

                case '\\':
                    sb.append("\\\\");
                    break;

                case '/':
                    sb.append("\\/");
                    break;

                case '\377':
                    // 0xff has been seen on recent Simrad VHF systems, and it seems to indicate
                    // end-of-field, with noise following. Assume this does not break other systems.
                    break;

                default:
                    if (c >= ' ' && c <= '~') {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
