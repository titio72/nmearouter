package com.aboni.nmea.router.data.track;

import java.time.Instant;
import java.util.List;

public interface TripManagerX {

    void onTrackPoint(TrackEvent point) throws TripManagerException;

    Trip getTrip(Instant timestamp);

    Trip getTrip(int id);

    void deleteTrip(int id) throws TripManagerException;

    void setTripDescription(int id, String description) throws TripManagerException;

    List<Trip> getTrips(boolean sortDescendant);

    List<Trip> getTrips(int year, boolean sortDescendant);
}
