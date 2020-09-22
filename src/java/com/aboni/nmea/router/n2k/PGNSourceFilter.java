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

    private final Map<Integer, Integer> pgnSources;

    private final Log logger;

    public PGNSourceFilter(Log logger) {
        this.logger = logger;
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

    public boolean accept(int source, int pgn) {
        int acceptedSrc = pgnSources.getOrDefault(pgn, WHITE_LIST ? ACCEPT_ALL : REJECT_ALL);
        return (acceptedSrc == source || acceptedSrc == -1);
    }
}
