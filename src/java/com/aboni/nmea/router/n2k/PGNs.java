package com.aboni.nmea.router.n2k;

import com.aboni.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PGNs {

    private Log logger;

    private Map<Integer, PGNDef> pgnDefMap = new HashMap<>();

    public PGNs(String file, Log logger) throws PGNDefParseException {
        this.logger = logger;

        if (logger != null) {
            logger.info("Start loading PGN definitions");
        }
        StringBuilder sbf = new StringBuilder();
        try (FileReader r = new FileReader(file)) {
            BufferedReader bf = new BufferedReader(r);
            String line;
            while ((line = bf.readLine()) != null) {
                sbf.append(line + "\r");
            }
            JSONObject big = new JSONObject(sbf.toString());
            JSONArray jPgnDefinitions = big.getJSONArray("PGNs");
            for (Iterator<Object> i = jPgnDefinitions.iterator(); i.hasNext(); ) {
                JSONObject jDef = (JSONObject) i.next();
                createDef(jDef);
            }
        } catch (Exception e) {
            throw new PGNDefParseException("Cannot load pgn definitions", e);
        }

        if (logger != null) {
            logger.info(String.format("Loaded {%d} PGN definitions", pgnDefMap.size()));
        }

    }

    private void createDef(JSONObject jDef) throws PGNDefParseException {
        try {
            PGNDef p = new PGNDef(jDef);
            pgnDefMap.put(p.getPgn(), p);
        } catch (Exception e) {
            if (logger != null) {
                logger.error(String.format("Cannot load PGN {%s}", jDef));
            }
            throw new PGNDefParseException("Cannot load pgn definition", e);
        }
    }

    public PGNDef getPGN(int pgnId) {
        return pgnDefMap.getOrDefault(pgnId, null);
    }
}
