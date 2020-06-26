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
    }

    private static final DateTimeFormatter tsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));
    private boolean debug;

    public static class PGNDataParseException extends RuntimeException {
        PGNDataParseException(long pgn) {
            super(String.format("PGN %d is unsupported", pgn));
        }
    }

    public void setDebug() {
        debug = true;
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
        byte[] data;
        JSONObject canBoatJson;
    }

    private PGNDecoded pgnData;
    private PGNDef definition;

    public PGNParser(@NotNull PGNs pgnDefinitions, String pgnString) {
        parse(pgnDefinitions, pgnString);
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
        if (definition != null) {
            PGNDef.PGNFieldDef lastField = definition.getFields()[definition.getFields().length - 1];
            return (lastField.getBitOffset() + lastField.getBitLength()) > (pgnData.data.length * 8);
        } else {
            return false;
        }
    }

    public JSONObject getCanBoatJson() {
        if (definition == null) throw new PGNDataParseException(pgnData.pgn);
        if (pgnData.canBoatJson == null && !needMore() && (experimental || isSupported(pgnData.pgn))) {
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

    public void addMore(String s) {
        StringTokenizer tok = new StringTokenizer(s, ",", false);
        tok.nextToken(); // skip timestamp
        tok.nextToken(); // skip priority
        tok.nextToken(); // skip pgn
        tok.nextToken(); // skip src
        tok.nextToken(); // skip dst
        int moreLen = Integer.parseInt(tok.nextToken());
        byte[] newData = new byte[pgnData.data.length + moreLen];
        System.arraycopy(pgnData.data, 0, newData, 0, pgnData.data.length);
        for (int i = 0; i < moreLen; i++) {
            String v = tok.nextToken();
            int c = Integer.parseInt(v, 16);
            newData[i + pgnData.length] = (byte) (c & 0xFF);
        }
        pgnData.data = newData;
        pgnData.length += moreLen;
    }

    private void parse(PGNs pgnDefinitions, String s) {
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

        PGNDef def = pgnDefinitions.getPGN(p.pgn);
        if (def != null) {
            definition = def;
        }

        this.pgnData = p;
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
                    case "Binary data":
                        res = parseBin(data, json, def);
                        break;
                    default:
                        res = parseValue(data, json, def);
                }
            }
            if (debug) ServerLog.getConsoleOut().println("[" + res + "]");
        }
        return json;
    }

    private static String parseBin(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        byte[] b = new byte[length / 8];
        for (int i = 0; i < length; i += 8) {
            long e = BitUtils.extractBits(data, start, offset + i, 8, false).v;
            b[i / 8] = (byte) e;
        }
        String res = new String(b);
        json.put(def.getName(), res);
        return res;
    }

    private static String parseEnum(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, def.isSigned());
        String ret = null;
        //if (e.v!=e.m) {
        String enumDesc = def.getENumValue((int) e.v);
        if (enumDesc != null) {
            json.put(def.getName(), enumDesc);
            ret = enumDesc;
        } else {
            json.put(def.getName(), "" + e.v);
            ret = "" + e.v;
        }
        //}
        return ret;
    }

    private static String parseValue(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, def.isSigned());
        //if (e.v!=e.m) {
            /*if (def.getResolution() == 1) {
                json.put(def.getName(), e.v);
            } else {*/
        double vv = e.v / ((int) 1.0 / def.getResolution());
        if ("rad".equals(def.getUnits())) {
            vv = Utils.round(Math.toDegrees(vv), 1);
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
        //    }
        //}
        //return null;
    }
}
