package com.aboni.nmea.router.n2k;

import org.json.JSONObject;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class CANBOATStream {

    private static final long MAX_AGE = 750L;
    private static final long MIN_AGE = 250L;

    private class Payload {
        int hashcode;
        long timestamp;
    }

    private final Map<Integer, Payload> payloadMap;

    public class PGN {
        public int getPgn() {
            return pgn;
        }

        public JSONObject getFields() {
            return fields;
        }

        int pgn;
        JSONObject fields;
    }

    @Inject
    public CANBOATStream() {
        payloadMap = new HashMap<>();
    }

    public PGN getMessage(String sMessage) {
        try {
            N2KLightParser p = new N2KLightParser(sMessage);
            if (isSend(p.getPgn(), p.getTs(), p.getFields())) {
                PGN res = new PGN();
                res.pgn = p.getPgn();
                res.fields = new JSONObject(p.getFields());
                return res;
            }
            else return null;
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isSend(int pgn, long ts, String fields) {
        Payload p = payloadMap.getOrDefault(pgn, null);
        if (p == null) {
            p = new Payload();
            p.timestamp = ts;
            p.hashcode = fields.hashCode();
            payloadMap.put(pgn, p);
            return true;
        } else {
            int hash = fields.hashCode();
            // check for minimum age (active throttling) then check for maximum age or some changes
            if ((ts - MIN_AGE) > p.timestamp && ((ts - MAX_AGE) > p.timestamp || hash != p.hashcode)) {
                p.timestamp = ts;
                p.hashcode = hash;
                return true;
            }
        }
        return false;
    }
}
