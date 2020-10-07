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

package com.aboni.nmea.router;

import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class GPSSatsRepository {

    private GPSSatsRepository() {
    }

    private static class GPSSatBean implements GPSSat {
        private String date;
        private String orbit;
        private String signal;
        private String clock;
        private int prn;
        private int svn;
        private String name;

        @Override
        public int getPrn() {
            return prn;
        }

        @Override
        public int getSvn() {
            return svn;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDate() {
            return date;
        }

        @Override
        public String getOrbit() {
            return orbit;
        }

        @Override
        public String getSignal() {
            return signal;
        }

        @Override
        public String getClock() {
            return clock;
        }

    }

    private static final Map<Integer, GPSSat> sats = new HashMap<>();

    static {
        loadGPSSats();
    }

    private static void loadGPSSats() {
        try (FileReader reader = new FileReader(Constants.CONF_DIR + "/sats.csv")) {
            BufferedReader r = new BufferedReader(reader);
            @SuppressWarnings("UnusedAssignment") String line = r.readLine(); // skip header
            while ((line = r.readLine()) != null) {
                GPSSatBean sat = getSat(line);
                if (sat != null) sats.put(sat.prn, sat);
            }
        } catch (IOException e) {
            ThingsFactory.getInstance(Log.class).errorForceStacktrace(
                    LogStringBuilder.start("SatRepository").wO("load").toString(), e);
        }
    }

    private static GPSSatBean getSat(String line) {
        try {
            GPSSatBean sat = new GPSSatBean();
            StringTokenizer tok = new StringTokenizer(line, ",");
            sat.prn = Integer.parseInt(tok.nextToken());
            sat.svn = Integer.parseInt(tok.nextToken());
            sat.name = tok.nextToken();
            sat.date = tok.nextToken();
            sat.orbit = tok.nextToken();
            sat.signal = tok.nextToken();
            sat.clock = tok.nextToken();
            return sat;
        } catch (Exception e) {
            ThingsFactory.getInstance(Log.class).errorForceStacktrace(
                    LogStringBuilder.start("SatRepository").wO("load").wV("string", line).toString(), e);
            return null;
        }
    }

    public static GPSSat getSat(int prn) {
        return sats.getOrDefault(prn, null);
    }
}
