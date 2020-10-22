package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.MsgGNSSPosition;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.GNSS_POSITION_UPDATE_PGN;

public class N2KGNSSPositionUpdateImpl extends N2KMessageImpl implements MsgGNSSPosition {

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
        super(getDefaultHeader(GNSS_POSITION_UPDATE_PGN), data);
        fill();
    }

    public N2KGNSSPositionUpdateImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != GNSS_POSITION_UPDATE_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", GNSS_POSITION_UPDATE_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        sid = BitUtils.getByte(data, 0, 0xFF);

        Double dLat = BitUtils.parseDouble(data, 56, 64, 0.0000000000000001, true);
        Double dLon = BitUtils.parseDouble(data, 120, 64, 0.0000000000000001, true);
        if (dLat != null && dLon != null) position = new Position(dLat, dLon);

        altitude = BitUtils.parseDoubleSafe(data, 184, 64, 1e-06, true);
        nSV = BitUtils.getByte(data, 264 / 8, 0xFF);
        hdop = BitUtils.parseDoubleSafe(data, 272, 16, 0.01, true);
        pdop = BitUtils.parseDoubleSafe(data, 288, 16, 0.01, true);
        geoidalSeparation = BitUtils.parseDoubleSafe(data, 304, 32, 0.01, true);
        ageOfDgnssCorrections = BitUtils.parseDoubleSafe(data, 360, 16, 0.01, false);
        referenceStationId = (int) BitUtils.parseIntegerSafe(data, 348, 4, 12, 0x0FFF);
        referenceStations = (int) BitUtils.parseIntegerSafe(data, 336, 0, 8, 0xFF);

        int daysSince1970 = (int) BitUtils.parseIntegerSafe(data, 8, 0, 16, 0);
        double secsSinceMidnight = BitUtils.parseDoubleSafe(data, 24, 32, 0.0001, false);
        if (daysSince1970 > 0 && BitUtils.isValidDouble(secsSinceMidnight)) {
            timestamp = Instant.ofEpochMilli(0).atZone(ZoneId.of("UTC")).plusDays(daysSince1970).plusNanos((long) (secsSinceMidnight * 1e9)).toInstant();
        }

        gnssType = BitUtils.parseEnum(data, 248, 0, 4, N2KLookupTables.getTable(GNS));
        method = BitUtils.parseEnum(data, 252, 4, 4, N2KLookupTables.getTable(GNS_METHOD));
        integrity = BitUtils.parseEnum(data, 256, 0, 2, N2KLookupTables.getTable(GNS_INTEGRITY));
        referenceStationType = BitUtils.parseEnum(data, 344, 0, 4, N2KLookupTables.getTable(GNS));
    }

    @Override
    public int getSID() {
        return sid;
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
        return BitUtils.isValidDouble(hdop);
    }

    @Override
    public double getPDOP() {
        return pdop;
    }

    @Override
    public boolean isPDOP() {
        return BitUtils.isValidDouble(pdop);
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
        return String.format("PGN {%s} Source {%d} Position {%s} Sats {%d} HDOP {%.1f} PDOP {%.1f} GNSS {%s}",
                GNSS_POSITION_UPDATE_PGN, getHeader().getSource(), getPosition(), getNSatellites(),
                getHDOP(), getPDOP(), getGnssType());
    }
}
