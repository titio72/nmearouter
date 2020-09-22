package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KGNSSPositionUpdate;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KGNSSPositionUpdateImpl extends N2KMessageImpl implements N2KGNSSPositionUpdate {

    private int sid;
    private Instant timestamp;
    private Position position;
    private double altitude;
    private String gnssType;
    private String method;
    private String integrity;
    private int nSV;
    private double hdop;
    private double pdop;
    private double geoidalSeparation;
    private int referenceStations;
    private String referenceStationType;
    private int referenceStationId;
    private double ageOfDgnssCorrections;

    public N2KGNSSPositionUpdateImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KGNSSPositionUpdateImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dLat = parseDouble(data, 56, 64, 0.0000000000000001, true);
        Double dLon = parseDouble(data, 120, 64, 0.0000000000000001, true);
        if (dLat != null && dLon != null) position = new Position(dLat, dLon);

        altitude = parseDoubleSafe(data, 184, 64, 1e-06, true);
        nSV = getByte(data, 264 / 8, 0xFF);
        hdop = parseDoubleSafe(data, 272, 16, 0.01, true);
        pdop = parseDoubleSafe(data, 288, 16, 0.01, true);
        geoidalSeparation = parseDoubleSafe(data, 304, 32, 0.01, true);
        ageOfDgnssCorrections = parseDoubleSafe(data, 360, 16, 0.01, false);
        referenceStationId = (int) parseIntegerSafe(data, 348, 4, 12, 0x0FFF);
        referenceStations = (int) parseIntegerSafe(data, 336, 0, 8, 0xFF);

        int daysSince1970 = (int) parseIntegerSafe(data, 8, 0, 16, 0);
        double secsSinceMidnight = parseDoubleSafe(data, 24, 32, 0.0001, false);
        if (daysSince1970 > 0 && isValidDouble(secsSinceMidnight)) {
            timestamp = Instant.ofEpochMilli(0).atZone(ZoneId.of("UTC")).plusDays(daysSince1970).plusNanos((long) (secsSinceMidnight * 1e9)).toInstant();
        }

        gnssType = parseEnum(data, 248, 0, 4, N2KLookupTables.getTable(GNS));
        method = parseEnum(data, 252, 4, 4, N2KLookupTables.getTable(GNS_METHOD));
        integrity = parseEnum(data, 256, 0, 2, N2KLookupTables.getTable(GNS_INTEGRITY));
        referenceStationType = parseEnum(data, 344, 0, 4, N2KLookupTables.getTable(GNS));
    }

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public boolean isValidSID() {
        return isValidByte(sid);
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public double getAltitude() {
        return altitude;
    }

    @Override
    public String getGnssType() {
        return gnssType;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public String getIntegrity() {
        return integrity;
    }

    @Override
    public int getNSatellites() {
        return nSV;
    }

    @Override
    public double getHDOP() {
        return hdop;
    }

    @Override
    public boolean isHDOP() {
        return isValidDouble(hdop);
    }

    @Override
    public double getPDOP() {
        return pdop;
    }

    @Override
    public boolean isPDOP() {
        return isValidDouble(pdop);
    }

    @Override
    public double getGeoidalSeparation() {
        return geoidalSeparation;
    }

    @Override
    public int getReferenceStations() {
        return referenceStations;
    }

    @Override
    public String getReferenceStationType() {
        return referenceStationType;
    }

    @Override
    public int getReferenceStationId() {
        return referenceStationId;
    }

    @Override
    public double getAgeOfDgnssCorrections() {
        return ageOfDgnssCorrections;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} Lat {%s} Lon {%s} Sats {%d} HDOP {%.1f} PDOP {%.1f} GNSS {%s}",
                PGN, getHeader().getSource(),
                (getPosition() == null) ? "" : Utils.formatLatitude(getPosition().getLatitude()),
                (getPosition() == null) ? "" : Utils.formatLongitude(getPosition().getLongitude()),
                getNSatellites(),
                getHDOP(),
                getPDOP(),
                getGnssType());
    }
}
