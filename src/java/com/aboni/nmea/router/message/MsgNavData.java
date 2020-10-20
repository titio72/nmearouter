package com.aboni.nmea.router.message;

import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

public interface MsgNavData extends Message {

    int getSID();

    double getDTW();

    Position getWaypoint();

    int getOriginWaypointNo();

    int getDestinationWaypointNo();

    double getWaypointClosingVelocity();

    double getBTW();

    double getBearingFromOriginToDestination();

    Instant getETA();

    DirectionReference getBTWReference();

    String getCalculationType();

    boolean isArrived();

    boolean isPerpendicularCrossed();
}
