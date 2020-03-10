package com.aboni.nmea.router.track;

import org.json.JSONObject;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface TrackQueryManager {
    List<Trip> getTrips() throws TrackManagementException;

    void dropDay(LocalDate cDate) throws TrackManagementException;

    JSONObject getYearlyStats() throws TrackManagementException;

    interface Trip {
        int getTrip();

        Date getMinDate();

        Date getMaxDate();

        String getTripDescription();

        Set<Date> getDates();
    }
}
