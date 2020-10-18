package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.DirectionReference;
import com.aboni.nmea.router.message.MsgNavData;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.NAV_DATA;
import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.POSITION_UPDATE_RAPID;

public class N2KNavDataImpl extends N2KMessageImpl implements MsgNavData {

    private int sid;
    private double dtw;
    private Position waypoint;
    private int originWaypointNo;
    private int destinationWaypointNo;
    private double waypointClosingVelocity;
    private double btw;
    private double bearingFromOriginToDestination;
    private Instant tETA;
    private DirectionReference btwReference;
    private String calculationType;
    private boolean arrived;
    private boolean perpendicularCrossed;

    @Override
    public int getSID() {
        return sid;
    }

    @Override
    public double getDTW() {
        return dtw;
    }

    @Override
    public Position getWaypoint() {
        return waypoint;
    }

    @Override
    public int getOriginWaypointNo() {
        return originWaypointNo;
    }

    @Override
    public int getDestinationWaypointNo() {
        return destinationWaypointNo;
    }

    @Override
    public double getWaypointClosingVelocity() {
        return waypointClosingVelocity;
    }

    @Override
    public boolean isPerpendicularCrossed() {
        return perpendicularCrossed;
    }

    @Override
    public double getBTW() {
        return btw;
    }

    @Override
    public double getBearingFromOriginToDestination() {
        return bearingFromOriginToDestination;
    }

    @Override
    public Instant getETA() {
        return tETA;
    }

    @Override
    public DirectionReference getBtwReference() {
        return btwReference;
    }

    @Override
    public String getCalculationType() {
        return calculationType;
    }

    @Override
    public boolean isArrived() {
        return arrived;
    }


    public N2KNavDataImpl(byte[] data) throws PGNDataParseException {
        super(getDefaultHeader(NAV_DATA), data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != NAV_DATA)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", NAV_DATA, header.getPgn()));
        fill();
    }

    public N2KNavDataImpl(N2KMessageHeader header, byte[] data) {
        super(header, data);
        fill();
    }

    private void fill() {
        sid = getByte(data, 0, 0xFF);
        dtw = parseDoubleSafe(data, 8, 32, 0.01, false);
        btwReference = DirectionReference.valueOf((int) parseIntegerSafe(data, 40, 0, 2, 0));
        perpendicularCrossed = 1==parseIntegerSafe(data, 42, 2, 2, 0);
        arrived = 1==parseIntegerSafe(data, 44, 4, 2, 0);
        switch ((int) parseIntegerSafe(data, 46, 6, 2, 0xFF)) {
            case 0: calculationType = "Great CIrcle"; break;
            case 1: calculationType = "Rhumb Line"; break;
            default: calculationType = "Unknown";
        }
        bearingFromOriginToDestination = Math.toDegrees(parseDoubleSafe(data, 96, 16, 0.0001, false));
        btw = Math.toDegrees(parseDoubleSafe(data, 112, 16, 0.0001, false));
        originWaypointNo = (int) parseIntegerSafe(data, 128, 0, 32, 0xFF);
        destinationWaypointNo = (int) parseIntegerSafe(data, 160, 0, 32, 0xFF);
        double lat = parseDoubleSafe(data, 192, 32, 0.0000001, true);
        double lon = parseDoubleSafe(data, 224, 32, 0.0000001, true);
        waypoint = (Double.isNaN(lat) || Double.isNaN(lon))?null:new Position(lat, lon);
        waypointClosingVelocity = parseDoubleSafe(data, 256, 16, 0.01, true) * 3600.0 / 1852.0;

        Long lDate = parseInteger(data, 80, 16);
        Double dTime = parseDouble(data, 48, 32, 0.0001, false);

        if (lDate != null && dTime != null && !dTime.isNaN()) {
            Instant i = Instant.ofEpochMilli(0);
            tETA = i.atZone(ZoneId.of("UTC")).plusDays(lDate).plusNanos((long) (dTime * 1000000000L)).toInstant();
        } else {
            tETA = null;
        }
    }

}
