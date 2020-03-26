package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.data.track.Trip;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

class TripImpl implements Trip {
    private final int tripId;
    private String desc;
    private Instant startTS;
    private Instant endTS;
    private double distance;

    TripImpl(int id, String desc) {
        this.desc = desc;
        this.tripId = id;
    }

    void setDistance(double d) {
        distance = d;
    }

    void addDistance(double d) {
        distance += d;
    }

    void setTS(Instant ts) {
        startTS = (startTS == null || ts.isBefore(startTS)) ? ts : startTS;
        endTS = (endTS == null || ts.isAfter(endTS)) ? ts : endTS;
    }

    @Override
    public int getTrip() {
        return tripId;
    }

    @Override
    public LocalDate getMinDate() {
        return startTS.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public LocalDate getMaxDate() {
        return endTS.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public String getTripDescription() {
        return desc;
    }

    public void setTripDescription(String description) {
        desc = description;
    }

    @Override
    public int countDays() {
        return (int) Duration.between(getMinDate().atStartOfDay(), getMaxDate().atStartOfDay()).toDays() + 1;
    }

    @Override
    public boolean checkTimestamp(Instant t) {
        return (startTS != null && startTS.getEpochSecond() <= t.toEpochMilli() &&
                (endTS == null || endTS.toEpochMilli() >= t.toEpochMilli()));
    }

    @Override
    public List<LocalDate> getDates() {
        int dd = countDays();
        List<LocalDate> res = new ArrayList<>(dd);
        for (int i = 0; i <= dd; i++) {
            res.add(getMinDate().plusDays(i));
        }
        return res;
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public long getTotalTime() {
        return endTS.toEpochMilli() - startTS.toEpochMilli();
    }

    @Override
    public Instant getStartTS() {
        return startTS;
    }

    @Override
    public Instant getEndTS() {
        return endTS;
    }
}
