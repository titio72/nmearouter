package com.aboni.nmea.router.track.impl;

import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackQueryManager;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBTrackQueryManager implements TrackQueryManager {

    private static class Trip implements TrackQueryManager.Trip {
        final int tripId;
        final Set<Date> dates = new TreeSet<>();
        final String desc;
        Date min;
        Date max;

        Trip(int id, String desc) {
            this.desc = desc;
            this.tripId = id;
        }

        void addDate(Date d) {
            if (max == null || max.compareTo(d) < 0) max = d;
            if (min == null || min.compareTo(d) > 0) min = d;
            dates.add(d);
        }

        @Override
        public int getTrip() {
            return tripId;
        }

        @Override
        public Date getMinDate() {
            return min;
        }

        @Override
        public Date getMaxDate() {
            return max;
        }

        @Override
        public String getTripDescription() {
            return desc;
        }

        @Override
        public Set<Date> getDates() {
            return Collections.unmodifiableSet(dates);
        }
    }

    private static final String SQL = "select track.tripId, Date(track.TS), (select trip.description from trip where trip.id=track.tripId) as description from track group by track.tripid, Date(track.TS)";

    private final DateFormat shortDateFormatter = new SimpleDateFormat("dd/MM");

    private void addToTrip(Map<Integer, TrackQueryManager.Trip> trips, Date d, Integer id, String desc) {
        Trip t = (Trip) trips.getOrDefault(id, null);
        if (t == null) {
            t = new Trip(id, desc);
            trips.put(id, t);
        }
        t.addDate(d);
    }

    private List<TrackQueryManager.Trip> sortIt(Map<Integer, TrackQueryManager.Trip> trips) {
        List<TrackQueryManager.Trip> tripList = new ArrayList<>(trips.values());
        tripList.sort((TrackQueryManager.Trip o1, TrackQueryManager.Trip o2) -> o2.getMinDate().compareTo(o1.getMinDate()));
        return tripList;
    }

    @Override
    public List<TrackQueryManager.Trip> getTrips() throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            int counter = 0;
            Map<Integer, TrackQueryManager.Trip> tripsDates = new TreeMap<>();
            try (PreparedStatement stm = db.getConnection().prepareStatement(SQL)) {
                readDays(counter, tripsDates, stm);
            }
            ServerLog.getLogger().info("Loaded {" + tripsDates.size() + "} trips from database");
            return sortIt(tripsDates);
        } catch (SQLException | ClassNotFoundException e) {
            throw new TrackManagementException(e);
        }
    }

    private void readDays(int counter, Map<Integer, TrackQueryManager.Trip> tripsDates, PreparedStatement stm) throws SQLException {
        try (ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                Date d = rs.getDate(2);
                int i = rs.getInt(1);
                String desc = rs.getString(3);
                if (i == 0) {
                    counter--;
                }
                addToTrip(tripsDates, d, (i == 0) ? counter : i, desc == null ? "" : desc);
            }
        }
    }

    @Override
    public void dropDay(@NotNull Calendar cDate) throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement stm = db.getConnection().prepareStatement("delete from track where Date(TS)=?")) {
                stm.setDate(1, new java.sql.Date(cDate.getTimeInMillis()));
                int i = stm.executeUpdate();
                ServerLog.getLogger().info("Removed date from track {" + shortDateFormatter.format(new Date(cDate.getTimeInMillis())) + "} " +
                        "samples {" + i + "}");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new TrackManagementException("Error deleting date", e);
        }
    }

    @Override
    public JSONObject getYearlyStats() throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement stm = db.getConnection().prepareStatement(SQL)) {
                JSONObject res = new JSONObject();
                JSONArray samples = new JSONArray();
                try (ResultSet rs = stm.executeQuery()) {
                    int lastM = 0;
                    int lastY = 0;
                    while (rs.next()) {
                        int y = rs.getInt(1);
                        if (lastY < y && lastY > 0) {
                            for (int i = lastM + 1; i <= 12; i++) {
                                JSONArray e = new JSONArray(new Object[]{lastY, i, 0.0, 0, 0});
                                samples.put(e);
                            }
                            lastM = 0;
                        }
                        lastY = y;

                        int m = rs.getInt(2);
                        if ((m - lastM) > 1) {
                            for (int i = lastM + 1; i < m; i++) {
                                JSONArray e = new JSONArray(new Object[]{y, i, 0.0, 0, 0});
                                samples.put(e);
                            }
                        }
                        lastM = m;
                        double dist = rs.getDouble(3);
                        double sailTime = rs.getDouble(4);
                        double days = rs.getDouble(5);
                        JSONArray e = new JSONArray(new Object[]{y, m, dist, sailTime / 3600, days});
                        samples.put(e);
                    }
                }
                res.put("NM_per_month", samples);
                return res;
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new TrackManagementException("Error reading distance stats", e);
        }
    }

}
