package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.CANBOATPGNMessage;
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
public class N2KLightParser implements CANBOATPGNMessage {

    public boolean isValid() {
        if (jFields == null && sFields != null && !invalid) {
            try {
                jFields = new JSONObject(sFields);
                invalid = false;
            } catch (JSONException e) {
                jFields = null;
                invalid = true;
            }
        }
        return !invalid;
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

    public N2KLightParser(String ss) {
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
