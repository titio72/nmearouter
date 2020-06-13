package com.aboni.nmea.router.n2k;

import com.aboni.utils.Log;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CANBOATStream {

    private static final long MAX_AGE = 750L;
    private static final long MIN_AGE = 250L;

    private class Payload {
        int hashcode;
        long timestamp;
    }

    private final Map<Integer, Integer> pgnSources;

    private final Map<Integer, Payload> payloadMap;

    public class PGN {
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

    private Log logger;

    public CANBOATStream(@NotNull Log logger) {
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
                String[] c = l.split(",");
                try {
                    int pgn = Integer.parseInt(c[0]);
                    int src = Integer.parseInt(c[1]);
                    pgnSources.put(pgn, src);
                } catch (Exception e) {
                    logger.error("Error reading pgn source mapping {" + l + "}");
                }
            }
        } catch (IOException e) {
            logger.error("Error reading pgn source mapping", e);
        }
    }

    public PGN getMessage(String sMessage) {
        try {
            N2KLightParser p = new N2KLightParser(sMessage);
            int pgn = p.getPgn();
            int src = pgnSources.getOrDefault(pgn, -1);
            if ((src==p.getSource() || src==-1) && (p.getPgn()==129025 || isSend(p.getPgn(), p.getTs(), p.getFields()))) {
                PGN res = new PGN();
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
