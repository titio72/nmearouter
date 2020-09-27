package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBImpl;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBPartAImpl;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBPartBImpl;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEAAISAgent extends NMEAAgentImpl implements AISTargets {

    private static final long CLEAN_UP_TIMEOUT = 30 * 60 * 1000L; //30 minutes

    private static final boolean TEST = false;

    public static class PositionReport {

        private final long timestamp;
        private final AISPositionReport report;

        private PositionReport(long timestamp, @NotNull AISPositionReport report) {
            this.report = report;
            this.timestamp = timestamp;
        }

        public String getMMSI() {
            return report.getMMSI();
        }

        public AISPositionReport getReport() {
            return report;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    private final Map<String, PositionReport> reports = new HashMap<>();
    private final Map<String, AISStaticData> data = new HashMap<>();

    @Inject
    public NMEAAISAgent(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(false, true);
        if (TEST) {
            try {
                N2KMessageParser p = ThingsFactory.getInstance(N2KMessageParser.class);
                p.addString(STATIC[0]);
                p.addString(STATIC[1]);
                p.addString(STATIC[2]);
                p.addString(STATIC[3]);
                onMessage(p.getMessage());

                p = ThingsFactory.getInstance(N2KMessageParser.class);
                p.addString(STATIC1[0]);
                p.addString(STATIC1[1]);
                p.addString(STATIC1[2]);
                p.addString(STATIC1[3]);
                p.addString(STATIC1[4]);
                onMessage(p.getMessage());

            } catch (Exception e) {
                getLogger().error("Cannot load test data", e);
            }
        }
    }

    @OnN2KMessage
    public void onMessage(N2KMessage message) {
        if (message instanceof AISPositionReport) {
            String mmsi = ((AISPositionReport) message).getMMSI();
            ((AISPositionReport) message).setOverrideTime(getCache().getNow());
            synchronized (reports) {
                reports.put(mmsi, new PositionReport(getCache().getNow(), (AISPositionReport) message));
            }
        }

        if (message instanceof AISStaticData) {
            AISStaticData s = (AISStaticData) message;
            String mmsi = s.getMMSI();
            String aisClass = s.getAISClass();
            if ("B".equals(aisClass)) {
                synchronized (data) {
                    if (!data.containsKey(mmsi)) {
                        data.put(mmsi, new N2KAISStaticDataBImpl());
                    }
                }
                AISStaticData d = data.get(mmsi);
                if (s instanceof N2KAISStaticDataBPartAImpl)
                    ((N2KAISStaticDataBImpl) d).setPartA((N2KAISStaticDataBPartAImpl) s);
                else if (s instanceof N2KAISStaticDataBPartBImpl)
                    ((N2KAISStaticDataBImpl) d).setPartB((N2KAISStaticDataBPartBImpl) s);
            } else {
                synchronized (data) {
                    data.put(mmsi, (AISStaticData) message);
                }
            }
        }
    }

    @Override
    public String getType() {
        return "AIS";
    }

    @Override
    public void onTimer() {
        loadTests();
        long now = getCache().getNow();
        synchronized (reports) {
            Collection<PositionReport> reportsCopy = new ArrayList<>(reports.values());
            for (PositionReport rep : reportsCopy) {
                if ((now - rep.getTimestamp()) > CLEAN_UP_TIMEOUT) {
                    reports.remove(rep.getMMSI());
                }
            }
        }
    }

    @Override
    public AISStaticData getData(String mmsi) {
        synchronized (data) {
            return data.getOrDefault(mmsi, null);
        }
    }

    @Override
    public List<AISPositionReport> getAISTargets() {
        List<AISPositionReport> res = new ArrayList<>();
        synchronized (reports) {
            for (PositionReport r : reports.values()) {
                res.add(r.getReport());
            }
        }
        return res;
    }

    int c;

    private void loadTests() {
        if (TEST) {
            synchronized (this) {
                c = (c + 1) % tests.length;
                try {
                    String[] s = tests[c];
                    N2KMessageParser p = ThingsFactory.getInstance(N2KMessageParser.class);
                    for (String ss : s) p.addString(ss);
                    N2KMessage m = p.getMessage();
                    onMessage(m);
                } catch (PGNDataParseException e) {
                    getLogger().errorForceStacktrace("Cannot process n2k message", e);
                }
            }
        }
    }

    private static final String[][] tests = new String[][]{
            {"2020-06-21-08:15:42.595,4,129038,0,255,8,a0,1b,c1,b8,68,bc,0e,31",
                    "2020-06-21-08:15:42.595,4,129038,0,255,8,a1,95,f7,05,de,5d,a9,19",
                    "2020-06-21-08:15:42.595,4,129038,0,255,8,a2,98,16,13,60,03,00,00",
                    "2020-06-21-08:15:42.596,4,129038,0,255,8,a3,00,68,12,00,00,f0,fe"},

            {"2020-06-21-08:15:44.846,4,129038,0,255,8,c0,1b,c1,80,0d,c3,0e,dd",
                    "2020-06-21-08:15:44.846,4,129038,0,255,8,c1,7b,f8,05,cd,96,b0,19",
                    "2020-06-21-08:15:44.846,4,129038,0,255,8,c2,a5,e0,87,27,03,00,00",
                    "2020-06-21-08:15:44.846,4,129038,0,255,8,c3,00,fd,86,00,00,f0,fe"},

            {"2020-06-21-08:14:44.664,4,129039,0,255,8,00,1a,12,2e,f1,bd,0e,7e",
                    "2020-06-21-08:14:44.664,4,129039,0,255,8,01,6c,ec,05,44,ba,af,19",
                    "2020-06-21-08:14:44.665,4,129039,0,255,8,02,ab,10,2a,c9,01,00,00",
                    "2020-06-21-08:14:44.665,4,129039,0,255,8,03,00,96,29,00,fc,ff,ff"},

            {"2020-06-21-08:14:52.582,4,129039,0,255,8,20,1a,12,e6,0c,03,0e,3a",
                    "2020-06-21-08:14:52.583,4,129039,0,255,8,21,ea,dc,05,66,0d,a9,19",
                    "2020-06-21-08:14:52.583,4,129039,0,255,8,22,20,ff,ff,00,00,03,00",
                    "2020-06-21-08:14:52.583,4,129039,0,255,8,23,08,ff,ff,00,fc,ff,ff"},

            {"2020-06-21-08:15:11.604,4,129041,0,255,8,e0,36,55,6d,e8,27,3b,4b",
                    "2020-06-21-08:15:11.608,4,129041,0,255,8,e1,ae,07,06,b4,03,f0,19",
                    "2020-06-21-08:15:11.609,4,129041,0,255,8,e2,fc,00,00,00,00,00,00",
                    "2020-06-21-08:15:11.609,4,129041,0,255,8,e3,ff,ff,5e,e0,ff,e1,1c",
                    "2020-06-21-08:15:11.609,4,129041,0,255,8,e4,01,4d,50,41,20,53,45",
                    "2020-06-21-08:15:11.614,4,129041,0,255,8,e5,43,43,48,45,20,44,45",
                    "2020-06-21-08:15:11.614,4,129041,0,255,8,e6,4c,4c,41,20,4d,45,4c",
                    "2020-06-21-08:15:11.614,4,129041,0,255,8,e7,20,20,20,41,2d,45,ff"},

            {"2020-06-21-08:15:07.013,4,129041,0,255,8,80,36,55,6b,e8,27,3b,6e",
                    "2020-06-21-08:15:07.015,4,129041,0,255,8,81,52,1a,06,10,7e,fa,19",
                    "2020-06-21-08:15:07.016,4,129041,0,255,8,82,fc,00,00,00,00,00,00",
                    "2020-06-21-08:15:07.016,4,129041,0,255,8,83,ff,ff,5e,e0,ff,e1,1c",
                    "2020-06-21-08:15:07.016,4,129041,0,255,8,84,01,4d,50,41,20,53,45",
                    "2020-06-21-08:15:07.016,4,129041,0,255,8,85,43,43,48,45,20,44,45",
                    "2020-06-21-08:15:07.017,4,129041,0,255,8,86,4c,4c,41,20,4d,45,4c",
                    "2020-06-21-08:15:07.017,4,129041,0,255,8,87,20,20,20,41,2d,43,ff"},

            {"2020-06-21-08:15:08.455,4,129041,0,255,8,a0,36,55,6a,e8,27,3b,54",
                    "2020-06-21-08:15:08.455,4,129041,0,255,8,a1,3c,15,06,40,bd,fe,19",
                    "2020-06-21-08:15:08.455,4,129041,0,255,8,a2,fc,00,00,00,00,00,00",
                    "2020-06-21-08:15:08.456,4,129041,0,255,8,a3,ff,ff,5e,e0,ff,e0,1c",
                    "2020-06-21-08:15:08.456,4,129041,0,255,8,a4,01,4d,50,41,20,53,45",
                    "2020-06-21-08:15:08.456,4,129041,0,255,8,a5,43,43,48,45,20,44,45",
                    "2020-06-21-08:15:08.457,4,129041,0,255,8,a6,4c,4c,41,20,4d,45,4c",
                    "2020-06-21-08:15:08.457,4,129041,0,255,8,a7,20,20,20,41,2d,42,ff"},

            {"2020-06-21-08:15:09.775,4,129041,0,255,8,c0,36,55,6c,e8,27,3b,6e",
                    "2020-06-21-08:15:09.775,4,129041,0,255,8,c1,52,1a,06,ee,e9,f2,19",
                    "2020-06-21-08:15:09.776,4,129041,0,255,8,c2,fc,00,00,00,00,00,00",
                    "2020-06-21-08:15:09.776,4,129041,0,255,8,c3,ff,ff,5e,e0,ff,e1,1c",
                    "2020-06-21-08:15:09.776,4,129041,0,255,8,c4,01,4d,50,41,20,53,45",
                    "2020-06-21-08:15:09.776,4,129041,0,255,8,c5,43,43,48,45,20,44,45",
                    "2020-06-21-08:15:09.777,4,129041,0,255,8,c6,4c,4c,41,20,4d,45,4c",
                    "2020-06-21-08:15:09.779,4,129041,0,255,8,c7,20,20,20,41,2d,44,ff"}
    };

    private static final String[] STATIC = new String[]{
            "2020-06-21-08:12:32.611,6,129809,0,255,8,20,19,18,e6,0c,03,0e,43",
            "2020-06-21-08:12:32.613,6,129809,0,255,8,21,4c,41,4e,20,53,4f,4c",
            "2020-06-21-08:12:32.613,6,129809,0,255,8,22,41,43,45,20,20,20,20",
            "2020-06-21-08:12:32.613,6,129809,0,255,8,23,20,20,20,20,20,ff,ff"
    };

    private static final String[] STATIC1 = new String[]{
            "2020-06-21-08:12:37.012,6,129810,0,255,8,80,21,18,e6,0c,03,0e,24",
            "2020-06-21-08:12:37.014,6,129810,0,255,8,81,00,00,00,43,53,4f,21",
            "2020-06-21-08:12:37.017,6,129810,0,255,8,82,32,44,50,49,38,20,20",
            "2020-06-21-08:12:37.017,6,129810,0,255,8,83,78,00,28,00,14,00,50",
            "2020-06-21-08:12:37.017,6,129810,0,255,8,84,00,00,00,00,00,03,ff"
    };
}
