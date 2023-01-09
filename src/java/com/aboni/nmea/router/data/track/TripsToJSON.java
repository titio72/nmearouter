/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data.track;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class TripsToJSON {
    private final List<Trip> tripList;
    private LocalDate minDate;
    private LocalDate maxDate;

    public TripsToJSON(List<Trip> trips) {
        this.tripList = (trips==null)?new ArrayList<>():trips;
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
            if (minDate == null || minDate.isAfter(t.getMinDate())) minDate = t.getMinDate();
            if (maxDate == null || maxDate.isBefore(t.getMaxDate())) maxDate = t.getMaxDate();
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
        if (start!=null) {
            trip.put("start", start.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            trip.put("startISO", start.toString());
        }
        if (end!=null) {
            trip.put("end", end.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)));
            trip.put("endISO", end.toString());
        }
        trip.put("nOfDays", n);
    }
}
