package com.aboni.nmea.router.agent.impl.track;

import com.aboni.utils.Pair;

public interface TripManager {

    Pair<Integer, Long> getCurrentTrip(long now);

    void setTrip(long from, long to, int tripId);


}
