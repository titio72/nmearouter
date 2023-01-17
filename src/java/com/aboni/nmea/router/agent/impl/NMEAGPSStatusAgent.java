package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.*;
import com.aboni.nmea.router.message.*;
import com.aboni.nmea.router.utils.Log;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
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

        private void update(MsgSOGAdCOG msg, long now) {
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
    private final List<SatInfo> satellites = new ArrayList<>();
    private long lastSatTime;
    private int nSat;

    @Inject
    public NMEAGPSStatusAgent(Log log, TimestampProvider tp) {
        super(log, tp, false, true);
    }

    @OnRouterMessage
    public void onMessage(RouterMessage rm) {
        if (rm != null && rm.getMessage() != null) {
            Message message = rm.getMessage();
            if (message instanceof MsgSystemTime) {
                handleSystemTime((MsgSystemTime) message);
            } else if (message instanceof MsgPositionAndVector) {
                handleSOGMessage((MsgPositionAndVector) message);
                handlePositionMessage((MsgPositionAndVector) message);
            } else if (message instanceof MsgSatellites) {
                handleSatellitesMessage((MsgSatellites) message);
            } else if (message instanceof MsgGNSSDOPs) {
                handleDOPs((MsgGNSSDOPs) message);
            }
        }
    }

    private void handleDOPs(MsgGNSSDOPs message) {
        synchronized (this) {
            setHDOP(message.getHDOP());
            setGPSFix(message.getFixDescription());

        }
    }

    private void handleSystemTime(MsgSystemTime message) {
        synchronized (this) {
            timestamp = message.getTime();
        }
    }

    private void handlePositionMessage(MsgPositionAndVector message) {
        synchronized (this) {
            if (message.getPosition() != null) {
                setPosition(message.getTimestamp(), message.getPosition());
            }
        }
    }

    private void handleSatellitesMessage(MsgSatellites message) {
        synchronized (satellites) {
            lastSatTime = getTimestampProvider().getNow();
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

    private void handleSOGMessage(MsgSOGAdCOG message) {
        sogAndCog.update(message, getTimestampProvider().getNow());
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
        super.onTimer();
        long now = getTimestampProvider().getNow();
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
