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

package com.aboni.nmea.router.n2k.canboat;

import com.aboni.nmea.router.n2k.N2KLookupTables;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PGNDef {

    private static final String S_SINGLE = "Single";
    private static final String S_ISO = "ISO";
    private static final String S_FAST = "Fast";
    private static final String S_UNKNOWN = "Unknown";

    private static final String S_ENUM = "Lookup table";
    private static final String S_BINARY = "Binary data";
    private static final String S_VALUE = "Value";

    enum PGNType {
        SINGLE(S_SINGLE),
        ISO(S_ISO),
        FAST(S_FAST),
        UNKNOWN(S_UNKNOWN);

        String id;

        PGNType(String s) {
            id = s;
        }

        public String getType() {
            return id;
        }

        static PGNType ofValue(String s) {
            switch (s) {
                case S_SINGLE:
                    return SINGLE;
                case S_FAST:
                    return FAST;
                case S_ISO:
                    return ISO;
                default:
                    return UNKNOWN;
            }
        }
    }

    enum PGNFieldType {
        ENUM(S_ENUM),
        BINARY(S_BINARY),
        VALUE(S_VALUE);

        String id;

        PGNFieldType(String s) {
            id = s;
        }

        public String getType() {
            return id;
        }

        static PGNFieldType ofValue(String s) {
            switch (s) {
                case S_ENUM:
                    return ENUM;
                case S_BINARY:
                    return BINARY;
                default:
                    return VALUE;
            }
        }
    }

    public static class PGNFieldDef {
        private final int order;
        private final String id;
        private final String name;
        private final int bitLength;
        private final int bitOffset;
        private final int bitStart;
        private final String type;
        private final boolean signed;
        private Map<Integer, String> values;
        private final String units;
        private final double resolution;

        PGNFieldDef(JSONObject definition) {
            if (definition.has("Field")) definition = definition.getJSONObject("Field");
            order = definition.has("Order") ? definition.getInt("Order") : 0;
            id = definition.getString("Id");
            name = definition.getString("Name");
            bitLength = definition.getInt("BitLength");
            bitStart = definition.getInt("BitStart");
            bitOffset = definition.has("BitOffset") ? definition.getInt("BitOffset") : 0;
            type = check(definition, "Type") ? definition.getString("Type") : "Unknown";
            units = check(definition, "Units") ? definition.getString("Units") : null;
            resolution = check(definition, "Resolution") ? definition.getDouble("Resolution") : 1.0;
            values = new HashMap<>();
            if (definition.has("EnumValues")) {
                JSONArray jValues = definition.getJSONArray("EnumValues");
                for (int i = 0; i < jValues.length(); i++) {
                    values.put(
                            Integer.parseInt(jValues.getJSONObject(i).getString("value")),
                            jValues.getJSONObject(i).getString("name")
                    );
                }
            } else {
                values = N2KLookupTables.getTable(id);
            }
            signed = definition.has("Signed") && definition.getBoolean("Signed");
        }

        private static boolean check(JSONObject j, String attr) {
            return j.has(attr) && !j.isNull(attr);
        }

        public int getOrder() {
            return order;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getBitLength() {
            return bitLength;
        }

        public int getBitOffset() {
            return bitOffset;
        }

        public int getBitStart() {
            return bitStart;
        }

        public String getType() {
            return type;
        }

        public boolean isSigned() {
            return signed;
        }

        public Map<Integer, String> getValues() {
            return values;
        }

        public String getENumValue(int i) {
            return values.getOrDefault(i, null);
        }

        public String getUnits() {
            return units;
        }

        public double getResolution() {
            return resolution;
        }
    }

    private final int pgn;
    private final String id;
    private final String description;
    private final PGNType type;
    private final boolean complete;
    private final int length;
    private final int repeatingFields;
    private final PGNFieldDef[] fields;

    public int getPgn() {
        return pgn;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public PGNType getType() {
        return type;
    }

    public boolean isComplete() {
        return complete;
    }

    public int getLength() {
        return length;
    }

    public int getRepeatingFields() {
        return repeatingFields;
    }

    public PGNFieldDef[] getFields() {
        return fields;
    }

    public PGNDef(JSONObject definition) {
        pgn = definition.getInt("PGN");
        id = definition.getString("Id");
        description = definition.getString("Description");
        type = PGNType.ofValue(definition.getString("Type"));
        complete = definition.getBoolean("Complete");
        length = definition.getInt("Length");
        repeatingFields = definition.has("RepeatingFields") ? definition.getInt("RepeatingFields") : 0;

        if (definition.has("Fields")) {
            Object jf = definition.get("Fields");
            if (jf instanceof JSONArray) {
                JSONArray jFields = (JSONArray) jf;
                int nFields = jFields.length();
                fields = new PGNFieldDef[nFields];
                for (int i = 0; i < nFields; i++) {
                    JSONObject jField = jFields.getJSONObject(i);
                    fields[i] = new PGNFieldDef(jField);
                }
            } else if (jf instanceof JSONObject) {
                fields = new PGNFieldDef[1];
                fields[0] = new PGNFieldDef((JSONObject) jf);
            } else {
                fields = null;
            }
        } else {
            fields = null;
        }
    }
}
