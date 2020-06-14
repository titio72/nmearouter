package com.aboni.nmea.router.n2k;

import com.aboni.utils.Log;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CANBOATStream {

    private static final boolean WHITE_LIST = false;
    private static final int ACCEPT_ALL = -1;
    private static final int REJECT_ALL = -99;

    private static final long MAX_AGE = 750L;
    private static final long MIN_AGE = 250L;

    private class Payload {
        int hashcode;
        long timestamp;
    }

    private final Map<Integer, Integer> pgnSources;

    private final Map<Integer, Payload> payloadMap;

    public class PGNMessage {
        public int getPgn() {
            return pgn;
        }

        public JSONObject getFields() {
            return fields;
        }

        public int getSource() {
            return source;
        }

        int source;
        int pgn;
        JSONObject fields;
    }

    private Log logger = null;

    public CANBOATStream(Log logger) {
        this.logger = logger;
        payloadMap = new HashMap<>();
        pgnSources = new HashMap<>();
        loadSources();
    }

    private void loadSources() {
        try (FileReader r = new FileReader("conf/pgns.csv")) {
            BufferedReader bf = new BufferedReader(r);
            String l;
            while ((l=bf.readLine())!=null) {
                loadSourceLine(l, pgnSources, logger);
            }
        } catch (IOException e) {
            if (logger!=null) logger.error("Error reading pgn source mapping", e);
        }
    }

    private static void loadSourceLine(String l, Map<Integer, Integer> pgnSources, Log logger) {
        try {
            String[] c = l.split(",");
            int pgn = Integer.parseInt(c[0]);
            int src = Integer.parseInt(c[1]);
            pgnSources.put(pgn, src);
        } catch (Exception e) {
            if (logger!=null) logger.error("Error reading pgn source mapping {" + l + "}");
        }
    }

    public PGNMessage getMessage(String sMessage) {
        try {
            N2KLightParser p = new N2KLightParser(sMessage);
            int pgn = p.getPgn();
            int src = pgnSources.getOrDefault(pgn, WHITE_LIST?ACCEPT_ALL:REJECT_ALL);
            if ((src==p.getSource() || src==-1) && (p.getPgn()==129025 || isSend(p.getPgn(), p.getTs(), p.getFields()))) {
                PGNMessage res = new PGNMessage();
                res.pgn = p.getPgn();
                res.source = p.getSource();
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
