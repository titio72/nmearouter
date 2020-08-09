package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KGNSSPositionUpdate extends N2KMessageImpl {

    public static final int PGN = 129029;


    public N2KGNSSPositionUpdate(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KGNSSPositionUpdate(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

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


    private void fill() {
        sid = getByte(data, 0, 0xFF);

        Double dLat = parseDouble(data, 56, 0, 64, 0.0000000000000001, true);
        Double dLon = parseDouble(data, 120, 0, 64, 0.0000000000000001, true);
        if (dLat != null && dLon != null) position = new Position(dLat, dLon);

        altitude = parseDoubleSafe(data, 184, 0, 64, 1e-06, true);
        nSV = getByte(data, 264 / 8, 0xFF);
        hdop = parseDoubleSafe(data, 272, 0, 16, 0.01, true);
        pdop = parseDoubleSafe(data, 288, 0, 16, 0.01, true);
        geoidalSeparation = parseDoubleSafe(data, 304, 0, 32, 0.01, true);
        ageOfDgnssCorrections = parseDoubleSafe(data, 360, 0, 16, 0.01, false);
        referenceStationId = (int) parseIntegerSafe(data, 348, 4, 12, false, 0x0FFF);
        referenceStations = (int) parseIntegerSafe(data, 336, 0, 8, false, 0xFF);

        int daysSince1970 = (int) parseIntegerSafe(data, 8, 0, 16, false, 0);
        double secsSinceMidnight = parseDoubleSafe(data, 24, 0, 32, 0.0001, false);
        if (daysSince1970 > 0 && isValidDouble(secsSinceMidnight)) {
            timestamp = Instant.ofEpochMilli(0).atZone(ZoneId.of("UTC")).plusDays(daysSince1970).plusNanos((long) (secsSinceMidnight * 1e9)).toInstant();
        }

        gnssType = parseEnum(data, 248, 0, 4, N2KLookupTables.getTable(GNS));
        method = parseEnum(data, 252, 4, 4, N2KLookupTables.getTable(GNS_METHOD));
        integrity = parseEnum(data, 256, 0, 2, N2KLookupTables.getTable(GNS_INTEGRITY));
        referenceStationType = parseEnum(data, 344, 0, 4, N2KLookupTables.getTable(GNS));
    }

    public int getSID() {
        return sid;
    }

    public boolean isValidSID() {
        return isValidByte(sid);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Position getPosition() {
        return position;
    }

    public double getAltitude() {
        return altitude;
    }

    public boolean isValidAltitude() {
        return isValidDouble(altitude);
    }

    public String getGnssType() {
        return gnssType;
    }

    public String getMethod() {
        return method;
    }

    public String getIntegrity() {
        return integrity;
    }

    public int getNSatellites() {
        return nSV;
    }

    public boolean isValidNSatellites() {
        return isValidByte(nSV);
    }

    public double getHDOP() {
        return hdop;
    }

    public boolean isHDOP() {
        return isValidDouble(hdop);
    }

    public double getPDOP() {
        return pdop;
    }

    public boolean isPDOP() {
        return isValidDouble(pdop);
    }

    public double getGeoidalSeparation() {
        return geoidalSeparation;
    }

    public boolean isValidGeoidalSeparation() {
        return isValidDouble(geoidalSeparation);
    }

    public int getReferenceStations() {
        return referenceStations;
    }

    public String getReferenceStationType() {
        return referenceStationType;
    }

    public int getReferenceStationId() {
        return referenceStationId;
    }

    public double getAgeOfDgnssCorrections() {
        return ageOfDgnssCorrections;
    }

    public boolean isValidAgeOfDgnssCorrections() {
        return isValidDouble(ageOfDgnssCorrections);
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
