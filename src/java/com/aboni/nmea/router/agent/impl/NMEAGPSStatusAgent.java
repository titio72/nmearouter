package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.GPSStatus;
import com.aboni.nmea.router.OnN2KMessage;
import com.aboni.nmea.router.SatInfo;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.Satellite;
import com.aboni.nmea.router.n2k.messages.*;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NMEAGPSStatusAgent extends NMEAAgentImpl implements GPSStatus {

    private static class SOGAndCOG {
        private long lastSogTime;
        private double sog;
        private double cog;

        private void reset() {
            synchronized (this) {
                sog = Double.NaN;
                cog = Double.NaN;
            }
        }

        private void update(N2KSOGAdCOGRapid msg, long now) {
            synchronized (this) {
                lastSogTime = now;
                sog = msg.getSOG();
                cog = msg.getCOG();
            }
        }
    }

    private String gpxFix = "Unknown";
    private double hdop;

    private GeoPositionT position;
    private Instant timestamp;

    private final SOGAndCOG sogAndCog = new SOGAndCOG();

    private final TimestampProvider timestampProvider;

    private final List<SatInfo> satellites = new ArrayList<>();
    private long lastSatTime;
    private int nSat;

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    public static final long STATIC_DEFAULT_PERIOD = 10L * MINUTE;
    public static final long DEFAULT_PERIOD = 30L * SECOND;

    @Inject
    public NMEAGPSStatusAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.timestampProvider = tp;
    }

    @OnN2KMessage
    public void onMessage(N2KMessage message) {
        if (message != null) {
            int pgn = message.getHeader().getPgn();
            switch (pgn) {
                case N2kMessagePGNs.SYSTEM_TIME_PGN:
                    handleSystemTime((N2KSystemTime) message);
                    break;
                case N2kMessagePGNs.GNSS_POSITION_UPDATE_PGN:
                    handlePositionMessage((N2KGNSSPositionUpdate) message);
                    break;
                case N2kMessagePGNs.SOG_COG_RAPID_PGN:
                    handleSOGMessage((N2KSOGAdCOGRapid) message);
                    break;
                case N2kMessagePGNs.SATELLITES_IN_VIEW_PGN:
                    handleSatellitesMessage((N2KSatellites) message);
                    break;
                case N2kMessagePGNs.GNSS_DOP_PGN:
                    handleDOPs((N2KGNSSDOPs) message);
                    break;
                default:
                    break;
            }
        }
    }

    private void handleDOPs(N2KGNSSDOPs message) {
        synchronized (this) {
            setHDOP(message.getHDOP());
            setGPSFix(message.getFixDescription());

        }
    }

    private void handleSystemTime(N2KSystemTime message) {
        synchronized (this) {
            timestamp = message.getTime();
        }
    }

    private void handlePositionMessage(N2KGNSSPositionUpdate message) {
        synchronized (this) {
            if (message.getPosition() != null) {
                setPosition(message.getTimestamp(), message.getPosition());
            }
        }
    }

    private void handleSatellitesMessage(N2KSatellites message) {
        synchronized (satellites) {
            lastSatTime = timestampProvider.getNow();
            satellites.clear();
            nSat = 0;
            for (Satellite s : message.getSatellites()) {
                SatInfo si = new SatInfo(String.format("%02d", s.getId()), s.getElevation(), s.getAzimuth(), s.getSrn(), "Used".equalsIgnoreCase(s.getStatus()));
                satellites.add(si);
                if (si.isUsed()) nSat++;
            }
            if (nSat == 0) {
                position = null;
                setGPSFix("no GNSS");
                setHDOP(99);
                sogAndCog.reset();
            }
        }
    }

    private void handleSOGMessage(N2KSOGAdCOGRapid message) {
        sogAndCog.update(message, timestampProvider.getNow());
    }

    private void setPosition(Instant timestamp, Position pos) {
        position = new GeoPositionT(timestamp.toEpochMilli(), pos);
    }

    private void setGPSFix(String fix) {
        gpxFix = fix;
    }

    private void setHDOP(double hdop) {
        this.hdop = hdop;
    }

    @Override
    public String getGPSFix() {
        return gpxFix;
    }

    @Override
    public double getHDOP() {
        return hdop;
    }

    @Override
    public GeoPositionT getPosition() {
        synchronized (this) {
            return position;
        }
    }

    @Override
    public double getCOG() {
        synchronized (sogAndCog) {
            return sogAndCog.cog;
        }
    }

    @Override
    public double getSOG() {
        synchronized (sogAndCog) {
            return sogAndCog.sog;
        }
    }

    @Override
    public Instant getPositionTime() {
        synchronized (this) {
            return (position != null) ? position.getInstant() : timestamp;
        }
    }

    @Override
    public int getUsedSatellites() {
        synchronized (satellites) {
            return nSat;
        }
    }

    @Override
    public List<SatInfo> getSatellites() {
        synchronized (satellites) {
            return new ArrayList<>(satellites);
        }
    }

    @Override
    public void onTimer() {
        long now = timestampProvider.getNow();
        if ((now - sogAndCog.lastSogTime) > 10000) {
            sogAndCog.reset();
        }

        if ((now - lastSatTime) > 10000) {
            synchronized (satellites) {
                satellites.clear();
                nSat = 0;
            }
        }
    }

    @Override
    public String getType() {
        return "GPSStatus";
    }
}
