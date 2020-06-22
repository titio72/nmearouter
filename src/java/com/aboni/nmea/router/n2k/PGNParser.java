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
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.StringTokenizer;

public class PGNParser {

    public static class PGNDataParseException extends Exception {
        PGNDataParseException(String msg) {
            super(msg);
        }

        PGNDataParseException(String msg, Throwable cause) {
            super(msg, cause);
        }
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

    public JSONObject getCanBoatJson() {
        return pgnData.canBoatJson;
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

        JSONObject res = new JSONObject();
        res.put("pgn", p.pgn);
        res.put("prio", p.priority);
        res.put("src", p.source);
        res.put("dst", p.dest);
        PGNDef def = pgnDefinitions.getPGN(p.pgn);

        if (def != null) {
            res.put("description", def.getDescription());
            PGNDef.PGNFieldDef[] fieldDefinitions = def.getFields();
            if (fieldDefinitions != null) {
                JSONObject jF = parse(fieldDefinitions, p.data);
                res.put("fields", jF);
            }
        }
        p.canBoatJson = res;
        this.pgnData = p;
    }

    private Instant parseTimestamp(String time) {
        //2011-11-24-22:42:04.437
        String sTs = time.substring(0, 10) + "T" + time.substring(11) + "Z";
        return Instant.parse(sTs);
    }

    private static JSONObject parse(PGNDef.PGNFieldDef[] fields, byte[] data) {
        JSONObject json = new JSONObject();
        for (PGNDef.PGNFieldDef def : fields) {
            if (!"reserved".equals(def.getId())) {
                switch (def.getType()) {
                    case VALUE:
                        parseValue(data, json, def);
                        break;
                    case ENUM:
                        parseEnum(data, json, def);
                        break;
                    case BINARY:
                    default:
                }
            }
        }
        return json;
    }

    private static void parseEnum(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        long e = BitUtils.extractBits(data, offset + start, length, def.isSigned());
        String desc = def.getValues()[(int) e];
        json.put(def.getName(), desc);
    }

    private static void parseValue(byte[] data, JSONObject json, PGNDef.PGNFieldDef def) {
        int offset = def.getBitOffset();
        int length = def.getBitLength();
        int start = def.getBitStart();
        long v = BitUtils.extractBits(data, offset + start, length, def.isSigned());
        if (def.getResolution() == 1) {
            json.put(def.getName(), v);
        } else {
            double vv = v * def.getResolution();
            if ("rad".equals(def.getUnits())) {
                json.put(def.getName(), Utils.round(Math.toDegrees(vv), 1));
            } else {
                json.put(def.getName(), vv);
            }

        }
    }

}
