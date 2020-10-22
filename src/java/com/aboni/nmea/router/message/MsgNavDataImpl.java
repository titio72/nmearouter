package com.aboni.nmea.router.message;

import com.aboni.misc.Utils;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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

    @Override
    public JSONObject toJSON() {
        JSONObject res = new JSONObject();

        res.put("topic", "nav");
        if (getWaypoint() != null) {
            JSONObject jWaypoint = new JSONObject();
            jWaypoint.put("latitude", Utils.formatLatitude(getWaypoint().getLatitude()));
            jWaypoint.put("longitude", Utils.formatLongitude(getWaypoint().getLongitude()));
            jWaypoint.put("dec_latitude", getWaypoint().getLatitude());
            jWaypoint.put("dec_longitude", getWaypoint().getLongitude());
            if (getDestinationWaypointNo() != 0xFF) jWaypoint.put("no", getDestinationWaypointNo());
            res.put("waypoint", jWaypoint);
        }
        if (!Double.isNaN(getWaypointClosingVelocity())) res.put("vmg", getWaypointClosingVelocity());
        if (!Double.isNaN(getBTW())) res.put("btw", getBTW());
        if (!Double.isNaN(getDTW())) res.put("dtw", getDTW());
        if (!Double.isNaN(getBearingFromOriginToDestination())) res.put("bearingOriginDest", getBTW());
        if (getOriginWaypointNo() != 0xFF) res.put("originWptNo", getBTW());
        res.put("arrived", isArrived());
        res.put("perpendicular_crossed", isPerpendicularCrossed());
        Duration d = Duration.between(Instant.now(), getETA());
        res.put("time_to_dest",
                String.format("%02dh %02dm %02ds", d.toHours(), d.toMinutes() % 60, (d.toMillis() / 1000) % 60));
        res.put("eta_utc", getETA().toString());
        res.put("eta_time", getETA().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)));
        res.put("eta_date", getETA().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE));

        return res;
    }
}
