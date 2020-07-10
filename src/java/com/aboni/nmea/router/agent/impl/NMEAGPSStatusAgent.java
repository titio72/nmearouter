package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.GPSStatus;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnN2KMessage;
import com.aboni.nmea.router.data.track.PositionStats;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.impl.N2KGNSSPositionUpdate;
import com.aboni.nmea.router.n2k.impl.N2KSOGAdCOGRapid;
import com.aboni.nmea.router.n2k.impl.N2KSatellites;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NMEAGPSStatusAgent extends NMEAAgentImpl implements GPSStatus {

    public static class SatInfo {

        public String getId() {
            return id;
        }

        public int getElevation() {
            return elevation;
        }

        public int getAzimuth() {
            return azimuth;
        }

        public int getNoise() {
            return noise;
        }

        public boolean isUsed() {
            return used;
        }

        private SatInfo(String id, int elevation, int azimuth, int noise, boolean used) {
            this.id = id;
            this.elevation = elevation;
            this.azimuth = azimuth;
            this.noise = noise;
            this.used = used;
        }

        private String id;
        private int elevation;
        private int azimuth;
        private int noise;
        private boolean used;
    }

    public enum GPSFix {
        UNKNOWN,
        NO_FIX,
        FIX_2D,
        FIX_3D
    }

    private GPSFix gpxFix;
    private double hdop;

    private GeoPositionT position;
    private final PositionStats stats;
    private final StationaryManager stationaryManager;

    private final Object sogSync = new Object();
    private long lastSogTime;
    private double sog;
    private double cog;

    private final List<SatInfo> satellites = new ArrayList<>();
    private long lastSatTime;
    private int nSat;

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;

    public static final long STATIC_DEFAULT_PERIOD = 10L * MINUTE;
    public static final long DEFAULT_PERIOD = 30L * SECOND;

    private static final long STATIC_THRESHOLD_TIME = 15L * MINUTE; // if static for more than x minutes set anchor mode

    private static final double MOVE_THRESHOLD_SPEED_KN = 3.0; // if reported is greater than X then it's moving
    private static final double MOVE_THRESHOLD_POS_METERS = 35.0; // if move by X meters since last reported point then it's moving

    @Inject
    public NMEAGPSStatusAgent(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, true);
        stats = new PositionStats();
        stationaryManager = new StationaryManager();
        stationaryManager.init();
    }

    @OnN2KMessage
    public void onMessage(N2KMessage message) {
        int pgn = message.getHeader().getPgn();
        switch (pgn) {
            case N2KGNSSPositionUpdate.PGN:
                handlePositionMessage((N2KGNSSPositionUpdate) message);
                break;
            case N2KSOGAdCOGRapid.PGN:
                handleSOGMessage((N2KSOGAdCOGRapid) message);
                break;
            case N2KSatellites.PGN:
                handleSatellitesMessage((N2KSatellites) message);
                break;
            default:
                break;
        }
    }

    private void handlePositionMessage(N2KGNSSPositionUpdate message) {
        synchronized (stats) {
            N2KGNSSPositionUpdate u = message;
            if (u.getPosition() != null) {
                onPosition(u.getTimestamp(), u.getPosition());
                setHDOP(u.getHDOP());
                String fix = u.getMethod();
                switch (fix) {
                    case "no GNSS":
                        setGPSFix(GPSFix.NO_FIX);
                        break;
                    case "GNSS fix":
                        setGPSFix(GPSFix.FIX_2D);
                        break;
                    case "Precise GNSS":
                        setGPSFix(GPSFix.FIX_3D);
                        break;
                    default:
                        setGPSFix(GPSFix.UNKNOWN);
                }
            }
        }
    }

    private void handleSatellitesMessage(N2KSatellites message) {
        N2KSatellites ss = message;
        synchronized (satellites) {
            lastSatTime = getCache().getNow();
            satellites.clear();
            nSat = 0;
            for (N2KSatellites.Sat s : ss.getSatellites()) {
                SatInfo si = new SatInfo(String.format("%02d", s.getId()), s.getElevation(), s.getAzimuth(), s.getSrn(), "Used".equalsIgnoreCase(s.getStatus()));
                satellites.add(si);
                if (si.isUsed()) nSat++;
            }
        }
    }

    private void handleSOGMessage(N2KSOGAdCOGRapid message) {
        N2KSOGAdCOGRapid s = message;
        synchronized (sogSync) {
            lastSogTime = getCache().getNow();
            setSOG(s.getSOG());
            setCOG(s.getCOG());
        }
    }

    private void onPosition(Instant timestamp, Position pos) {
        GeoPositionT p = new GeoPositionT(timestamp.toEpochMilli(), pos);
        stats.addPosition(p);

        boolean stationary = isStationary(p, position);
        stationaryManager.updateStationaryStatus(p.getTimestamp(), stationary);

        position = p;
    }

    private void setGPSFix(GPSFix fix) {
        this.gpxFix = fix;
    }

    private void setHDOP(double hdop) {
        this.hdop = hdop;
    }

    private void setSOG(double sog) {
        this.sog = sog;
    }

    private void setCOG(double cog) {
        this.cog = cog;
    }

    private boolean isStationary(GeoPositionT p1, GeoPositionT p2) {
        if (p1 == null || p2 == null) {
            return false;
        } else {
            double dist = p1.distanceTo(p2); // distance in meters
            long dTime = Math.abs(p2.getTimestamp() - p1.getTimestamp()); // d-time in milliseconds
            // calc the speed but only if the two points are at least 500ms apart
            double speed =
                    dTime > 500 ? ((dist / (double) dTime) * 1000.0) : 0.0; // meter/second
            speed *= 1.94384; // speed in knots
            return speed <= MOVE_THRESHOLD_SPEED_KN && dist < MOVE_THRESHOLD_POS_METERS;
        }
    }

    @Override
    public GPSFix getGPSFix() {
        return gpxFix;
    }

    @Override
    public double getHDOP() {
        return hdop;
    }

    @Override
    public GeoPositionT getPosition() {
        synchronized (stats) {
            return position;
        }
    }

    @Override
    public double getCOG() {
        synchronized (sogSync) {
            return cog;
        }
    }

    @Override
    public double getSOG() {
        synchronized (sogSync) {
            return sog;
        }
    }

    @Override
    public Instant getPositionTime() {
        synchronized (stats) {
            return (position != null) ? position.getInstant() : null;
        }
    }

    @Override
    public Position getAveragePosition() {
        synchronized (stats) {
            return stats.getAveragePosition();
        }
    }

    @Override
    public boolean isAnchor(long now) {
        synchronized (stats) {
            return stationaryManager.isAnchor(now);
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
            return Collections.unmodifiableList(satellites);
        }
    }

    private static class StationaryManager {

        boolean initialized = false;
        boolean stationary = true;
        long stationarySince = 0;

        void init() {
            if (!initialized) {
                this.stationary = true;
                initialized = true;
            }
        }

        boolean isAnchor(long t) {
            return stationary && ((t - stationarySince) > STATIC_THRESHOLD_TIME);
        }

        void updateStationaryStatus(long t, boolean stationary) {
            if (this.stationary != stationary) {
                this.stationarySince = stationary ? t : 0;
                this.stationary = stationary;
            }
        }
    }

    @Override
    public void onTimer() {
        long now = getCache().getNow();
        if ((now - lastSogTime) > 10000) {
            synchronized (sogSync) {
                sog = Double.NaN;
                cog = Double.NaN;
            }
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
        return "GPSSTatus";
    }
}
