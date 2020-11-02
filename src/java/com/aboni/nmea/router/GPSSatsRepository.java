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
        String[] satsRep = new String[] {
                "13,43,GPS 2R-2,1997/7/23,MEO,L1C/A,RB",
                "11,46,GPS 2R-3,1999/10/7,MEO,L1C/A,RB",
                "20,51,GPS 2R-4,2000/5/11,MEO,L1C/A,RB",
                "28,44,GPS 2R-5,2000/7/16,MEO,L1C/A,RB",
                "14,41,GPS 2R-6,2000/11/10,MEO,L1C/A,RB",
                "16,56,GPS 2R-8,2003/1/29,MEO,L1C/A,RB",
                "21,45,GPS 2R-9,2003/3/31,MEO,L1C/A,RB",
                "22,47,GPS 2R-10,2003/12/21,MEO,L1C/A,RB",
                "19,59,GPS 2R-11,2004/3/20,MEO,L1C/A,RB",
                "2,61,GPS 2R-13,2004/11/6,MEO,L1C/A,RB",
                "17,53,GPS 2R-14M,2005/9/26,MEO,L1C/A, L2C,RB",
                "31,52,GPS 2R-15M,2006/9/25,MEO,L1C/A, L2C,RB",
                "12,58,GPS 2R-16M,2006/11/17,MEO,L1C/A, L2C,RB",
                "15,55,GPS 2R-17M,2007/10/17,MEO,L1C/A, L2C,RB",
                "29,57,GPS 2R-18M,2007/12/20,MEO,L1C/A, L2C,RB",
                "7,48,GPS 2R-19M,2008/3/15,MEO,L1C/A, L2C,RB",
                "5,50,GPS 2R-21M,2009/8/17,MEO,L1C/A, L2C,RB",
                "25,62,GPS 2F-1,2010/5/28,MEO,L1C/A, L2C, L5,RB",
                "1,63,GPS 2F-2,2011/7/16,MEO,L1C/A, L2C, L5,RB",
                "24,65,GPS 2F-3,2012/10/4,MEO,L1C/A, L2C, L5,CS",
                "27,66,GPS 2F-4,2013/5/15,MEO,L1C/A, L2C, L5,RB",
                "30,64,GPS 2F-5,2014/2/21,MEO,L1C/A, L2C, L5,RB",
                "6,67,GPS 2F-6,2014/5/17,MEO,L1C/A, L2C, L5,RB",
                "9,68,GPS 2F-7,2014/8/2,MEO,L1C/A, L2C, L5,RB",
                "3,69,GPS 2F-8,2014/10/29,MEO,L1C/A, L2C, L5,RB",
                "26,71,GPS 2F-9,2015/3/25,MEO,L1C/A, L2C, L5,RB",
                "8,72,GPS 2F-10,2015/7/15,MEO,L1C/A, L2C, L5,CS",
                "10,73,GPS 2F-11,2015/10/31,MEO,L1C/A, L2C, L5,RB",
                "32,70,GPS 2F-12,2016/2/5,MEO,L1C/A, L2C, L5,RB",
                "4,74,GPS 3-1,2018/12/23,MEO,L1C/A, L2C, L5,RB",
                "18,75,GPS 3-2,2019/8/22,MEO,L1C/A, L2C, L5,RB"
        };
        for (String line : satsRep) {
            GPSSatBean sat = getSat(line);
            sats.put(sat.prn, sat);
        }
    }

    private static GPSSatBean getSat(String line) {
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
    }

    public static GPSSat getSat(int prn) {
        return sats.getOrDefault(prn, null);
    }

    /*
    "Header: PRN,SVN,Satellite,Launch Date (UTC),Orbit (*1),Positioning Signals,Clock(*2)
     */
}
