package com.aboni.nmea.router.services;

import com.aboni.nmea.router.track.Trip;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

class TripsToJSON {
    private final List<Trip> tripList;
    private LocalDate minDate;
    private LocalDate maxDate;

    TripsToJSON(List<Trip> trips) {
        this.tripList = trips;
    }

    public JSONObject go() {
        JSONObject res = new JSONObject();
        JSONArray trips = new JSONArray();
        res.put("trips", trips);

        int nDays = 0;
        long totDuration = 0L;
        double totalDistance = 0L;

        for (Trip t : tripList) {
            long duration = t.getTotalTime();
            double distance = t.getDistance();
            totDuration += duration;
            totalDistance += distance;
            if (minDate == null || minDate.compareTo(t.getMinDate()) > 0) minDate = t.getMinDate();
            if (maxDate == null || maxDate.compareTo(t.getMaxDate()) < 0) maxDate = t.getMaxDate();
            nDays += t.countDays();
            JSONObject trip = new JSONObject();
            trip.put("id", t.getTrip());
            trip.put("description", t.getTripDescription());
            fillTimeAndDistance(trip, duration, distance, t.getMinDate(), t.getMaxDate(), t.countDays());
            trip.put("startTS", t.getStartTS().toString());
            trip.put("endTS", t.getEndTS().toString());
            trips.put(trip);
        }
        JSONObject tot = new JSONObject();
        fillTimeAndDistance(tot, totDuration, totalDistance, minDate, maxDate, nDays);
        res.put("total", tot);

        return res;
    }

    private static void fillTimeAndDistance(JSONObject trip, long duration, double distance, LocalDate start, LocalDate end, int n) {
        long days = duration / (60 * 60 * 24 * 1000);
        duration = duration % (60 * 60 * 24 * 1000) / 1000L;
        trip.put("duration", (days == 0) ?
                String.format("%dh %02dm", duration / 3600, (duration % 3600) / 60) :
                String.format("%dd %dh %02dm", days, duration / 3600, (duration % 3600) / 60));
        trip.put("distance", String.format("%.2f", distance));
        trip.put("start", start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        trip.put("end", end.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
        trip.put("startISO", start.toString());
        trip.put("endISO", end.toString());
        trip.put("nOfDays", n);
    }
}
