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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.N2KStream;
import com.aboni.utils.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class N2KStreamImpl implements N2KStream {

    private static final boolean WHITE_LIST = false;
    private static final int ACCEPT_ALL = -1;
    private static final int REJECT_ALL = -99;

    private static final long MAX_AGE = 750L;
    private static final long MIN_AGE = 250L;

    private static final boolean THROTTLING = false;

    private static class Payload {
        int hashcode;
        long timestamp;
    }

    private final Map<Integer, Integer> pgnSources;
    private final Map<Integer, Payload> payloadMap;
    private final Log logger;

    public N2KStreamImpl(Log logger) {
        this.logger = logger;
        payloadMap = new HashMap<>();
        pgnSources = new HashMap<>();
        loadSources();
    }

    private void loadSources() {
        try (FileReader r = new FileReader(Constants.CONF_DIR + "/pgns.csv")) {
            BufferedReader bf = new BufferedReader(r);
            String l;
            while ((l = bf.readLine()) != null) {
                loadSourceLine(l, pgnSources, logger);
            }
        } catch (IOException e) {
            if (logger != null) logger.error("Error reading pgn source mapping", e);
        }
    }

    private static void loadSourceLine(String l, Map<Integer, Integer> pgnSources, Log logger) {
        try {
            String[] c = l.split(",");
            int pgn = Integer.parseInt(c[0]);
            int src = Integer.parseInt(c[1]);
            pgnSources.put(pgn, src);
        } catch (Exception e) {
            if (logger != null) logger.error("Error reading pgn source mapping {" + l + "}");
        }
    }

    @Override
    public N2KMessage getMessage(String sMessage) {
        try {
            N2KMessageParser p = new N2KMessageParser(sMessage);
            int pgn = p.getHeader().getPgn();
            int acceptedSrc = pgnSources.getOrDefault(pgn, WHITE_LIST ? ACCEPT_ALL : REJECT_ALL);
            if (p.isSupported() &&
                    (acceptedSrc == p.getHeader().getSource() || acceptedSrc == -1) &&
                    isSend(pgn, p.getHeader().getTimestamp().toEpochMilli(), p.getData())) {
                return p.getMessage();
            } else return null;
        } catch (Exception e) {
            if (logger != null) logger.error("N2KStream error for {" + sMessage + "} {" + e.toString() + "}", e);
            return null;
        }
    }

    private static int hashCodeOf(byte[] data) {
        return new String(data).hashCode();
    }

    private boolean isSend(int pgn, long ts, byte[] data) {
        if (THROTTLING) {
            Payload p = payloadMap.getOrDefault(pgn, null);
            if (p == null) {
                p = new Payload();
                p.timestamp = ts;
                p.hashcode = hashCodeOf(data);
                payloadMap.put(pgn, p);
                return true;
            } else {
                int hash = hashCodeOf(data);
                // check for minimum age (active throttling) then check for maximum age or some changes
                if ((ts - MIN_AGE) > p.timestamp &&
                        ((ts - MAX_AGE) > p.timestamp || hash != p.hashcode)) {
                    p.timestamp = ts;
                    p.hashcode = hash;
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
