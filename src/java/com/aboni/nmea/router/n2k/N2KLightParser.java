package com.aboni.nmea.router.n2k;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Light parser for the output of the canboat Analyzer JSON format.
 * See: https://github.com/canboat/canboat
 * It just extracts the timestamp, the pgn and the substring with the fields in the most efficient way.
 * Used to avoid parsing the entire JSON when the application does not need to process it.
 */
public class N2KLightParser {

    public long getTs() {
        return ts;
    }

    public int getPgn() {
        return pgn;
    }

    public int getSource() {
        return source;
    }

    public String getFields() {
        return sFields;
    }

    private long ts = 0L;
    private int pgn = 0;
    private String sFields = null;
    private int source = 0;

    private static final DateTimeFormatter timestampFormatter =
            DateTimeFormatter.ofPattern("uuuu-MM-dd-HH:mm:ss.SSS").withZone(ZoneId.of("UTC"));

    //{"timestamp":"2020-06-05-20:36:32.435","prio":2,"src":2,"dst":255,"pgn":129025,"description":"Position, Rapid Update","fields":{"Latitude":43.6774763,"Longitude":10.2739919}}

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
