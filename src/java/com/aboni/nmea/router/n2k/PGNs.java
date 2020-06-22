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
