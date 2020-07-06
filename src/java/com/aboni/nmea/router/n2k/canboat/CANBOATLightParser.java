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

import org.json.JSONException;
import org.json.JSONObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Light parser for the output of the canboat Analyzer JSON format.
 * See: https://github.com/canboat/canboat
 * It just extracts the timestamp, the pgn and the substring with the fields in the most efficient way.
 * Used to avoid parsing the entire JSON when the application does not need to process it.
 */
public class CANBOATLightParser implements CANBOATPGNMessage {

    public boolean isValid() {
        if (jFields == null && sFields != null && !invalid) {
            try {
                jFields = new JSONObject(patch129040And129039(sFields));
                invalid = false;
            } catch (JSONException e) {
                jFields = null;
                invalid = true;
            }
        }
        return !invalid;
    }

    private String patch129040And129039(String sFields) {
        if (pgn==129040 || pgn==129039) {
            sFields = sFields.replaceFirst("\"Regional Application\"", "Regional Application 1");
        }
        return sFields;
    }

    public long getTs() {
        return ts;
    }

    @Override
    public int getPgn() {
        return pgn;
    }

    @Override
    public int getSource() {
        return source;
    }

    public String getFieldsAsString() {
        return sFields;
    }

    @Override
    public JSONObject getFields() {
        if (isValid()) return jFields;
        else return null;
    }

    private long ts = 0L;
    private int pgn = 0;
    private String sFields = null;
    private int source = 0;
    private JSONObject jFields = null;
    private boolean invalid = false;

    private static final DateTimeFormatter timestampFormatter =
            DateTimeFormatter.ofPattern("uuuu-MM-dd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));

    public CANBOATLightParser(String ss) {
        ts = ZonedDateTime.parse(ss.substring(14, 37), timestampFormatter).toInstant().toEpochMilli();
        int i = 40;
        for (; i < ss.length() - 4; i++) {
            if (ss.charAt(i) == ',' && ss.regionMatches(i + 2, "src\"", 0, 3)) {
                source = Integer.parseInt(ss.substring(i + 7, ss.indexOf(',', i + 5)));
                i += 5;  // arbitrary...
                break;
            }
        }
        for (; i < ss.length() - 4; i++) {
            if (ss.charAt(i) == ',' && ss.regionMatches(i + 2, "pgn\"", 0, 3)) {
                pgn = Integer.parseInt(ss.substring(i + 7, ss.indexOf(',', i + 5)));
                i += 5;  // arbitrary...
                break;
            }
        }
        for (; i < ss.length() - 4; i++) {
            if (ss.charAt(i) == ',' && ss.regionMatches(i + 2, "fields\"", 0, 6)) {
                sFields = ss.substring(i + 10, ss.length() - 1);
                break;
            }
        }
    }
}
