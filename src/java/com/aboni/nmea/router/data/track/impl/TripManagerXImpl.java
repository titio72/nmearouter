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

package com.aboni.nmea.router.data.track.impl;

import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.SafeLog;
import com.aboni.nmea.router.utils.db.DBHelper;
import com.aboni.utils.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import java.sql.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class TripManagerXImpl implements TripManagerX {

    public static final int TRIM_PADDING_SECONDS = 300;
    public static final String ERROR_SAVING_TRIP = "Error saving trip";
    private final TripArchive archive;
    private final AtomicBoolean initialized;
    private final Log log;
    private final TripDAO tripDAO;
    private final TrackDAO trackDAO;

    @Inject
    public TripManagerXImpl(Log log, @Named(Constants.TAG_TRIP) String tripTableName, @Named(Constants.TAG_TRACK) String trackTableName) {
        initialized = new AtomicBoolean();
        archive = new TripArchive();
        this.log = SafeLog.getSafeLog(log);
        this.tripDAO = new TripDAO(tripTableName);
        this.trackDAO = new TrackDAO(trackTableName);
    }

    private static void throwUnknownTripException(int id) throws TripManagerException {
        throw new TripManagerException("Unknown trip " + id);
    }

    public void init() throws TripManagerException {
        if (!initialized.get()) {
            synchronized (archive) {
                loadArchive(archive);
                initialized.set(true);
            }
        }
    }

    private void loadArchive(TripArchive archive) throws TripManagerException {
        try (DBHelper helper = new DBHelper(log, true)) {
            tripDAO.loadArchive(archive::setTrip, helper.getConnection());
        } catch (SQLException | ClassNotFoundException | MalformedConfigurationException e) {
            throw new TripManagerException("Error loading trips in memory", e);
        }
    }

    TripImpl getCurrentTrip(Instant now) {
        Trip t = archive.getLastTrip();
        if (t != null && t.getEndTS().plus(3, ChronoUnit.HOURS).isAfter(now) && t.getStartTS().minusMillis(1).isBefore(now)) {
            return (TripImpl) t;
        } else {
            return null;
        }
    }

    @Override
    public void onTrackPoint(TrackEvent event) throws TripManagerException {
        init();
        synchronized (archive) {
            TrackPoint point = event.getPoint();
            TripImpl t = getCurrentTrip(point.getPosition().getInstant());
            double distToAdd = point.isAnchor() ? 0.0 : point.getDistance();
            if (t == null) {
                t = (TripImpl) archive.getNew();
                t.setTS(point.getPosition().getInstant());
                t.addDistance(distToAdd, point.getEngine());
                try (DBHelper db = new DBHelper(log, false)) {
                    try (Connection conn = db.getConnection()) {
                        tripDAO.saveTrip(t, conn);
                        archive.setTrip(t);
                        trackDAO.writeEvent(event, conn);
                        conn.commit();
                    }
                } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
                    throw new TripManagerException("Error starting a new trip from track point", e);
                }
            } else {
                try (DBHelper db = new DBHelper(log, false)) {
                    try (Connection conn = db.getConnection()) {
                        t.setTS(point.getPosition().getInstant());
                        t.addDistance(distToAdd, point.getEngine());
                        trackDAO.writeEvent(event, conn);
                        tripDAO.updateTrip(new TripEvent(t), conn);
                        conn.commit();
                    }
                } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
                    throw new TripManagerException("Error writing rack point", e);
                }
            }
        }
    }

    @Override
    public Trip getTrip(Instant timestamp) throws TripManagerException {
        init();
        return archive.getTrip(timestamp);
    }

    @Override
    public Trip getTrip(int id) throws TripManagerException {
        init();
        return archive.getTrip(id);
    }

    @Override
    public void deleteTrip(int id) throws TripManagerException {
        init();
        Trip t = getTrip(id);
        if (t == null) throwUnknownTripException(id);
        else {
            archive.delete(id);
            try (DBHelper db = new DBHelper(log, false)) {
                Connection c = db.getConnection();
                tripDAO.deleteTrip(id, c);
                trackDAO.deleteFromTrack(t.getStartTS(), t.getStartTS(), c, true);
                c.commit();
            } catch (ClassNotFoundException | MalformedConfigurationException | SQLException e) {
                throw new TripManagerException("Error deleting trip " + id, e);
            }
        }
    }

    @Override
    public void setTripDescription(int id, String description) throws TripManagerException {
        init();
        TripImpl t = (TripImpl) archive.getTrip(id);
        if (t == null) throwUnknownTripException(id);
        else {
            t.setTripDescription(description);
            try (DBHelper helper = new DBHelper(log, true); Connection c = helper.getConnection()) {
                tripDAO.saveTrip(t, c);
            } catch (SQLException | ClassNotFoundException | MalformedConfigurationException e) {
                throw new TripManagerException(ERROR_SAVING_TRIP, e);
            }
        }
    }

    @Override
    public List<Trip> getTrips(boolean desc) throws TripManagerException {
        init();
        synchronized (archive) {
            int sz = archive.tripsByDate.size();
            List<Trip> l = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                l.add(archive.tripsByDate.get(desc ? (sz - i - 1) : i));
            }
            return Collections.unmodifiableList(l);
        }
    }

    @Override
    public List<Trip> getTrips(int year, boolean desc) throws TripManagerException {
        init();
        synchronized (archive) {
            int sz = archive.tripsByDate.size();
            List<Trip> l = new ArrayList<>(sz);
            for (int i = 0; i < sz; i++) {
                Trip t = archive.tripsByDate.get(desc ? (sz - i - 1) : i);
                if (t.getMaxDate().getYear() == year || t.getMinDate().getYear() == year) l.add(t);
            }
            return Collections.unmodifiableList(l);
        }
    }

    @Override
    public void updateTripDistance(int id, double dist) throws TripManagerException {
        init();
        TripImpl t = (TripImpl) archive.getTrip(id);
        if (t == null) throwUnknownTripException(id);
        else {
            t.setDistance(dist);
            try (DBHelper helper = new DBHelper(log, true); Connection c = helper.getConnection()) {
                tripDAO.saveTrip(t, c);
            } catch (SQLException | ClassNotFoundException | MalformedConfigurationException e) {
                throw new TripManagerException(ERROR_SAVING_TRIP, e);
            }
        }
    }

    @Override
    public void trimTrip(int id) throws TripManagerException {
        init();
        Trip t = getTrip(id);
        if (t == null) throwUnknownTripException(id);
        else {
            try (DBHelper db = new DBHelper(log, false)) {
                TripImpl newT = getTrimmedTrip(t, db.getConnection());
                if (newT != null) {
                    archive.setTrip(newT);
                    Connection c = db.getConnection();
                    tripDAO.saveTrip(newT, c);
                    trackDAO.deleteFromTrack(t.getStartTS(), newT.getStartTS(), c, true);
                    trackDAO.deleteFromTrack(newT.getEndTS(), t.getEndTS(), c, false);
                    c.commit();
                }
            } catch (SQLException | ClassNotFoundException | MalformedConfigurationException e) {
                throw new TripManagerException(ERROR_SAVING_TRIP, e);
            }
        }
    }

    private TripImpl getTrimmedTrip(Trip t, Connection connection) throws SQLException {
        Pair<Instant, Instant> bounds = trackDAO.getTrimmedTrip(t.getStartTS(), t.getEndTS(), connection);
        if (bounds!=null) {
            TripImpl newT = new TripImpl(t.getTrip(), t.getTripDescription());
            newT.setTS(bounds.first.minusSeconds(TRIM_PADDING_SECONDS));
            newT.setTS(bounds.second.plusSeconds(TRIM_PADDING_SECONDS));
            return newT;
        } else {
            return null;
        }
    }

    private static class TripArchive {
        final Map<Integer, Trip> tripsMap = new HashMap<>();
        final List<Trip> tripsByDate = new ArrayList<>();

        Trip getNew() {
            synchronized (this) {
                int newId = 0;
                for (int i : tripsMap.keySet()) {
                    newId = Math.max(i, newId);
                }
                newId++;
                return new TripImpl(newId, "Trip " + newId);
            }
        }

        void setTrip(Trip t) {
            synchronized (this) {
                Trip existingTrip = tripsMap.getOrDefault(t.getTrip(), null);
                if (existingTrip == null) {
                    tripsMap.put(t.getTrip(), t);
                } else {
                    int position = Collections.binarySearch(tripsByDate, existingTrip, Comparator.comparing(Trip::getStartTS));
                    tripsByDate.remove(position);
                }
                int insertAt = -Collections.binarySearch(tripsByDate, t, Comparator.comparing(Trip::getStartTS)) - 1;
                if (insertAt == tripsByDate.size()) tripsByDate.add(t);
                else tripsByDate.add(insertAt, t);

            }
        }

        Trip getTrip(int id) {
            return tripsMap.getOrDefault(id, null);
        }

        int getPosition(Instant timestamp) {
            synchronized (this) {
                TripImpl temp = new TripImpl(-99999999, "");
                temp.setTS(timestamp);
                int position = Collections.binarySearch(tripsByDate, temp, Comparator.comparing(Trip::getStartTS));
                if (position < 0) {
                    int p = -position - 1;
                    if (p > 0) {
                        Trip t = tripsByDate.get(p - 1);
                        if (t.getEndTS().compareTo(timestamp) >= 0) return p - 1;
                    }
                }
                return position;
            }
        }

        Trip getTrip(Instant timestamp) {
            synchronized (this) {
                int position = getPosition(timestamp);
                if (position >= 0) {
                    return tripsByDate.get(position);
                } else {
                    return null;
                }
            }
        }

        Trip getLastTrip() {
            synchronized (this) {
                if (tripsByDate.isEmpty()) return null;
                else return tripsByDate.get(tripsByDate.size() - 1);
            }
        }

        void delete(int id) {
            synchronized (this) {
                Trip t = getTrip(id);
                if (t != null) {
                    int position = getPosition(t.getStartTS());
                    tripsByDate.remove(position);
                    tripsMap.remove(id);
                }
            }
        }
    }
}

