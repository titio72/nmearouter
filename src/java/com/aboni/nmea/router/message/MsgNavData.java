/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.message;

import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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


    @Override
    default JSONObject toJSON() {
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

    @Override
    default String getMessageContentType() {
        return "NavData";
    }

}
