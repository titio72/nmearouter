package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.*;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBImpl;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBPartAImpl;
import com.aboni.nmea.router.n2k.messages.impl.N2KAISStaticDataBPartBImpl;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.*;

public class NMEAAISAgent extends NMEAAgentImpl implements AISTargets {

    private static final long CLEAN_UP_TIMEOUT = 30 * 60 * 1000L; //30 minutes

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
    private final TimestampProvider timestampProvider;

    @Inject
    public NMEAAISAgent(@NotNull Log log, @NotNull TimestampProvider timestampProvider) {
        super(log, timestampProvider, false, true);
        this.timestampProvider = timestampProvider;
    }

    @OnRouterMessage
    public void onRouterMessage(RouterMessage m) {
        if (m.getMessage() instanceof N2KMessage) {
            onMessage((N2KMessage) m.getMessage());
        }
    }

    public void onMessage(N2KMessage message) {
        if (message instanceof AISPositionReport) {
            String mmsi = ((AISPositionReport) message).getMMSI();
            ((AISPositionReport) message).setOverrideTime(timestampProvider.getNow());
            synchronized (reports) {
                reports.put(mmsi, new PositionReport(timestampProvider.getNow(), (AISPositionReport) message));
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
        long now = timestampProvider.getNow();
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
}
