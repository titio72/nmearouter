package com.aboni.nmea.router.message;

import net.sf.marineapi.nmea.util.Position;
import java.time.Instant;

public class MsgNavDataImpl implements MsgNavData {

    private int sid = -1;
    private double dtw = Double.NaN;
    private double btw = Double.NaN;
    private Position waypoint = null;
    private int originWaypoint = 0xFF;
    private int destinationWaypoint = 0xFF;
    private double waypointClosingVelocity = Double.NaN;
    private double bearingOriginDestination = Double.NaN;
    private Instant eta = null;
    private DirectionReference btwReference = DirectionReference.TRUE;
    private boolean arrived = false;
    private boolean perpendicularCrossed = false;
    private String calcType = "Great Circle";

    public MsgNavDataImpl() {
        // nothing to do
    }

    public void setSID(int sid) {
        this.sid = sid;
    }

    public void setDTW(double dtw) {
        this.dtw = dtw;
    }

    public void setBTW(double btw) {
        this.btw = btw;
    }

    public void setWaypoint(Position waypoint) {
        this.waypoint = waypoint;
    }

    public void setOriginWaypointNo(int originWaypoint) {
        this.originWaypoint = originWaypoint;
    }

    public void setDestinationWaypointNo(int destinationWaypoint) {
        this.destinationWaypoint = destinationWaypoint;
    }

    public void setWaypointClosingVelocity(double waypointClosingVelocity) {
        this.waypointClosingVelocity = waypointClosingVelocity;
    }

    public void setBearingFromOriginToDestination(double bearingOriginDestination) {
        this.bearingOriginDestination = bearingOriginDestination;
    }

    public void setETA(Instant eta) {
        this.eta = eta;
    }

    public void setBTWReference(DirectionReference btwReference) {
        this.btwReference = btwReference;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public void setPerpendicularCrossed(boolean perpendicularCrossed) {
        this.perpendicularCrossed = perpendicularCrossed;
    }

    public void setCalculationType(String calcType) {
        this.calcType = calcType;
    }

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
        return originWaypoint;
    }

    @Override
    public int getDestinationWaypointNo() {
        return destinationWaypoint;
    }

    @Override
    public double getWaypointClosingVelocity() {
        return waypointClosingVelocity;
    }

    @Override
    public double getBTW() {
        return btw;
    }

    @Override
    public double getBearingFromOriginToDestination() {
        return bearingOriginDestination;
    }

    @Override
    public Instant getETA() {
        return eta;
    }

    @Override
    public DirectionReference getBTWReference() {
        return btwReference;
    }

    @Override
    public String getCalculationType() {
        return calcType;
    }

    @Override
    public boolean isArrived() {
        return arrived;
    }

    @Override
    public boolean isPerpendicularCrossed() {
        return perpendicularCrossed;
    }
}
