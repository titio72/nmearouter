package com.aboni.nmea.router.track;

import com.aboni.utils.Pair;

import java.time.LocalDate;
import java.util.List;

public interface TripManager {

    String getTripName(int tripId);

    /**
     * Loads the points of a track.
     *
     * @param trip The trip id.
     * @return A collection of track points.
     * @throws TripManagerException In case of DB issues.
     */
    List<TrackPoint> loadTrip(int trip) throws TripManagerException;

    /**
     * Get the "current" trip. A trip is current is the last sample is not older than a few hours.
     *
     * @param now The "now" timestamp.
     * @return The is of the current trip.
     * @throws TripManagerException In case of DB problems.
     */
    Pair<Integer, Long> getCurrentTrip(long now) throws TripManagerException;

    /**
     * Add to the trip all the track items from "from" to "to".
     *
     * @param from   Timestamp (UNIX time)
     * @param to     Timestamp (UNIX time)
     * @param tripId The is of trip to be set.
     * @throws TripManagerException In case of wrong timestamps or any DB problem
     */
    void setTrip(long from, long to, int tripId) throws TripManagerException;

    /**
     * Create a new trip.
     *
     * @return The id of newly created trip.
     * @throws TripManagerException
     */
    int createTrip() throws TripManagerException;

    /**
     * Add a entire day to a trip.
     *
     * @param trip The trip id.
     * @param date The date to add to the trip.
     * @throws TripManagerException
     */
    void addDateToTrip(int trip, LocalDate date) throws TripManagerException;

    /**
     * Update an existing trip's description.
     *
     * @param trip
     * @param description
     * @return True is a trip has been updated, false if no trip matching the given id ws found.
     * @throws TripManagerException
     */
    boolean setDescription(int trip, String description) throws TripManagerException;
}
