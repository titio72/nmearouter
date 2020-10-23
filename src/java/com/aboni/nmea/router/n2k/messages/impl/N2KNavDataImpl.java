package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.DirectionReference;
import com.aboni.nmea.router.message.MsgNavData;
import com.aboni.nmea.router.message.beans.MsgNavDataImpl;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;
import java.time.ZoneId;

import static com.aboni.nmea.router.n2k.messages.N2KMessagePGNs.NAV_DATA;

public class N2KNavDataImpl extends N2KMessageImpl implements MsgNavData {

    private final MsgNavDataImpl navData = new MsgNavDataImpl();

    @Override
    public int getSID() {
        return navData.getSID();
    }

    @Override
    public double getDTW() {
        return navData.getDTW();
    }

    @Override
    public Position getWaypoint() {
        return navData.getWaypoint();
    }

    @Override
    public int getOriginWaypointNo() {
        return navData.getOriginWaypointNo();
    }

    @Override
    public int getDestinationWaypointNo() {
        return navData.getDestinationWaypointNo();
    }

    @Override
    public double getWaypointClosingVelocity() {
        return navData.getWaypointClosingVelocity();
    }

    @Override
    public boolean isPerpendicularCrossed() {
        return navData.isPerpendicularCrossed();
    }

    @Override
    public double getBTW() {
        return navData.getBTW();
    }

    @Override
    public double getBearingFromOriginToDestination() {
        return navData.getBearingFromOriginToDestination();
    }

    @Override
    public Instant getETA() {
        return navData.getETA();
    }

    @Override
    public DirectionReference getBTWReference() {
        return navData.getBTWReference();
    }

    @Override
    public String getCalculationType() {
        return navData.getCalculationType();
    }

    @Override
    public boolean isArrived() {
        return navData.isArrived();
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
        navData.setSID(BitUtils.getByte(data, 0, 0xFF));
        navData.setDTW(BitUtils.parseDoubleSafe(data, 8, 32, 0.01, false) / 1852);
        navData.setBTWReference(DirectionReference.valueOf((int) BitUtils.parseIntegerSafe(data, 40, 0, 2, 0)));
        navData.setPerpendicularCrossed(1 == BitUtils.parseIntegerSafe(data, 42, 2, 2, 0));
        navData.setArrived(1 == BitUtils.parseIntegerSafe(data, 44, 4, 2, 0));
        switch ((int) BitUtils.parseIntegerSafe(data, 46, 6, 2, 0xFF)) {
            case 0:
                navData.setCalculationType("Great Circle");
                break;
            case 1:
                navData.setCalculationType("Rhumb Line");
                break;
            default:
                navData.setCalculationType("Unknown");
        }
        navData.setBearingFromOriginToDestination(Math.toDegrees(BitUtils.parseDoubleSafe(data, 96, 16, 0.0001, false)));
        navData.setBTW(Math.toDegrees(BitUtils.parseDoubleSafe(data, 112, 16, 0.0001, false)));
        navData.setOriginWaypointNo((int) BitUtils.parseIntegerSafe(data, 128, 0, 32, 0xFF));
        navData.setDestinationWaypointNo((int) BitUtils.parseIntegerSafe(data, 160, 0, 32, 0xFF));
        double lat = BitUtils.parseDoubleSafe(data, 192, 32, 0.0000001, true);
        double lon = BitUtils.parseDoubleSafe(data, 224, 32, 0.0000001, true);
        navData.setWaypoint((Double.isNaN(lat) || Double.isNaN(lon)) ? null : new Position(lat, lon));
        navData.setWaypointClosingVelocity(BitUtils.parseDoubleSafe(data, 256, 16, 0.01, true) * 3600.0 / 1852.0);

        Long lDate = BitUtils.parseInteger(data, 80, 16);
        Double dTime = BitUtils.parseDouble(data, 48, 32, 0.0001, false);

        if (lDate != null && dTime != null && !dTime.isNaN()) {
            Instant i = Instant.ofEpochMilli(0);
            navData.setETA(i.atZone(ZoneId.of("UTC")).plusDays(lDate).plusNanos((long) (dTime * 1000000000L)).toInstant());
        } else {
            navData.setETA(null);
        }
    }

    @Override
    public String toString() {
        return String.format("PGN {%d} Source {%d} Waypoint {%s} ETA {%s} DTW {%.1f} BTW {%.1f} vmg {%.1f}",
                NAV_DATA, getHeader().getSource(), getWaypoint(), getETA(), getDTW(), getBTW(), getWaypointClosingVelocity());
    }
}
