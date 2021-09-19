/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.n2k;

import com.aboni.nmea.router.Constants;
import com.aboni.utils.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PGNSourceFilter {

    private static final boolean WHITE_LIST = false;
    private static final int ACCEPT_ALL = -1;
    private static final int REJECT_ALL = -99;

    private static class PGNAndSource {
        int pgn;
        int source;

        static PGNAndSource of(int pgn, int source) {
            PGNAndSource x = new PGNAndSource();
            x.pgn = pgn;
            x.source = source;
            return x;
        }

        @Override
        public boolean equals(Object x) {
            if (x instanceof PGNAndSource) return ((PGNAndSource) x).pgn == pgn && ((PGNAndSource) x).source == source;
            else return false;
        }

        @Override
        public int hashCode() {
            return pgn * 256 + source;
        }
    }

    private final Map<Integer, Integer> pgnSources;
    private final Map<Integer, Integer> pgnSecondarySources;

    private final Map<PGNAndSource, Long> pgnLastTime;

    private final Log logger;

    public PGNSourceFilter(Log logger) {
        this.logger = logger;
        pgnSources = new HashMap<>();
        pgnSecondarySources = new HashMap<>();
        pgnLastTime = new HashMap<>();
        loadSources();
    }

    private void loadSources() {
        try (FileReader r = new FileReader(Constants.CONF_DIR + "/pgns.csv")) {
            BufferedReader bf = new BufferedReader(r);
            String l;
            while ((l = bf.readLine()) != null) {
                loadSourceLine(l);
            }
        } catch (IOException e) {
            if (logger != null) logger.error("Error reading pgn source mapping", e);
        }
    }

    private void loadSourceLine(String l) {
        try {
            String[] c = l.split(",");
            int pgn = Integer.parseInt(c[0]);
            int src = Integer.parseInt(c[1]);
            if (l.length() > 2) {
                int altSrc = Integer.parseInt(c[2]);
                pgnSecondarySources.put(pgn, altSrc);
            }
            pgnSources.put(pgn, src);
        } catch (Exception e) {
            if (logger != null) logger.error("Error reading pgn source mapping {" + l + "}");
        }
    }

    public void setPGNTimestamp(int source, int pgn, long time) {
        pgnLastTime.put(PGNAndSource.of(pgn, source), time);
    }

    public boolean accept(int source, int pgn, long now) {
        Integer primarySource = pgnSources.getOrDefault(pgn, null);
        if (primarySource == null) {
            // not supported
            return WHITE_LIST;
        } else if (primarySource == source || primarySource == -1) {
            // supported and the source is the primary source
            return true;
        } else {
            // supported but the source is not the primary source
            Integer secondarySource = pgnSecondarySources.getOrDefault(pgn, null);
            if (secondarySource == null) {
                // there is no secondary source - reject
                return false;
            } else if (secondarySource == -1 || secondarySource == source) {
                // the secondary source matches - check if the timeout expired for the primary source
                long lastTime = pgnLastTime.getOrDefault(PGNAndSource.of(pgn, primarySource), 0L);
                return (now - lastTime) > 10000;
            } else {
                return false;
            }
        }
    }

    public boolean accept(int source, int pgn) {
        int acceptedSrc = pgnSources.getOrDefault(pgn, WHITE_LIST ? ACCEPT_ALL : REJECT_ALL);
        return (acceptedSrc == source || acceptedSrc == -1);
    }
}
