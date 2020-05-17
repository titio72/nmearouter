package com.aboni.nmea.router.data.track;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface Trip {
    int getTrip();

    LocalDate getMinDate();

    LocalDate getMaxDate();

    Instant getStartTS();

    Instant getEndTS();

    String getTripDescription();

    List<LocalDate> getDates();

    double getDistance();

    long getTotalTime();

    int countDays();

    /**
     * Check if the trip was running at the given time
     *
     * @param t The time to be checked
     * @return true is at time t the trip was running, false otherwise.
     */
    boolean checkTimestamp(Instant t);

}
