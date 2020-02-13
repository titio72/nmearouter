package com.aboni.nmea.router.agent.impl.track;

import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.utils.db.Event;

public class TrackEvent implements Event {

    private final TrackPoint point;

    public TrackEvent(TrackPoint point) {
        this.point = point;
	}
	
	@Override
	public long getTime() {
        return point.getPosition().getTimestamp();
    }

    public TrackPoint getPoint() {
        return point;
	}

}
