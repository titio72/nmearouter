package com.aboni.utils.db;

import com.aboni.nmea.router.agent.impl.track.EngineStatus;
import com.aboni.utils.Pair;
import com.aboni.utils.ServerLog;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public class TrackAnalytics {

    static class MovingAverageMax {

        private double d;
        private String tag;
        private List<Pair<Long, Double>> sss = new LinkedList<>();
        private double distance = 0;

        private long t0Max = 0;
        private long t1Max = 0;
        private double speedMax = 0.0;

        MovingAverageMax(double distance, String tag) {
            d = distance;
            this.tag = tag;
        }

        void addSample(Pair<Long, Double> p) {
            sss.add(p);
            distance += p.second;
            if (distance > d) {
                // pull the first item from the rolling window
                Pair<Long, Double> p0 = sss.get(0);
                while (distance > d && !sss.isEmpty()) {
                    distance -= p0.second;
                    sss.remove(0);
                    p0 = sss.get(0);
                }

                long dT = p.first - p0.first;
                double speed = distance / ((double) dT / 3600000d);
                if (speed > speedMax) {
                    t0Max = p0.first;
                    t1Max = p.first;
                    speedMax = speed;
                }
            }
        }

        double getMaxSpeed() {
            return speedMax;
        }

        long getMaxSpeedT0() {
            return t0Max;
        }

        long getMaxSpeedT1() {
            return t1Max;
        }

        void fillRes(JSONObject result) {
            if (getMaxSpeed() > 0.0) {
                result.put("t0_" + tag, getMaxSpeedT0());
                result.put("t1_" + tag, getMaxSpeedT1());
                result.put("speed_" + tag, getMaxSpeed());
            }
        }
    }

    private static final String SQL = "select TS, dist, speed, maxSpeed, engine, anchor, dTime from track where TS>=? and TS<?";

    public class Stats {
        long samples;
        long totalNavigationTime = 0;
        long totalAnchorTime = 0;
        double totalNavigationDistance = 0;
        long[] sailEngineTime = new long[]{0, 0, 0};
        double[] sailEngineDistance = new double[]{0.0, 0.0, 0.0};
        double maxSpeed = 0.0;
        long maxSpeedTime = 0;
        double max30sAverageSpeed = 0.0;
        long max30sAverageSpeedTime = 0;
        double maxSampleAverageSpeed = 0.0;
        long maxSampleAverageSpeedTime = 0;
        MovingAverageMax[] movingAvgMaxes = new MovingAverageMax[]{
                new MovingAverageMax(1.0, "1NM"),
                new MovingAverageMax(5.0, "5NM"),
                new MovingAverageMax(10.0, "10NM")};

        public JSONObject toJson() {
            JSONObject j = new JSONObject();
            j.put("samples", samples);
            j.put("navTime", totalNavigationTime);
            j.put("navDist", totalNavigationDistance);
            j.put("navEngineOffTime", sailEngineTime[EngineStatus.OFF.getValue()]);
            j.put("navEngineOn_Time", sailEngineTime[EngineStatus.ON.getValue()]);
            j.put("navEngineUnkTime", sailEngineTime[EngineStatus.UNKNOWN.getValue()]);
            j.put("navEngineOffDist", sailEngineDistance[EngineStatus.OFF.getValue()]);
            j.put("navEngineOn_Dist", sailEngineDistance[EngineStatus.ON.getValue()]);
            j.put("navEngineUnkDist", sailEngineDistance[EngineStatus.UNKNOWN.getValue()]);
            j.put("anchorTime", totalAnchorTime);
            j.put("maxSpeed", maxSpeed);
            j.put("maxSpeedTime", maxSpeedTime);
            j.put("maxAvgSpeed", max30sAverageSpeed);
            j.put("maxAvgSpeedTime", max30sAverageSpeedTime);
            j.put("maxSampledSpeed", maxSampleAverageSpeed);
            j.put("maxSampledSpeedTime", maxSampleAverageSpeedTime);
            j.put("max01NMSpeed", movingAvgMaxes[0].getMaxSpeed());
            j.put("max01NMSpeedTime0", movingAvgMaxes[0].getMaxSpeedT0());
            j.put("max01NMSpeedTime1", movingAvgMaxes[0].getMaxSpeedT1());
            j.put("max05NMSpeed", movingAvgMaxes[1].getMaxSpeed());
            j.put("max05NMSpeedTime0", movingAvgMaxes[1].getMaxSpeedT0());
            j.put("max05NMSpeedTime1", movingAvgMaxes[1].getMaxSpeedT1());
            j.put("max10NMSpeed", movingAvgMaxes[2].getMaxSpeed());
            j.put("max10NMSpeedTime0", movingAvgMaxes[2].getMaxSpeedT0());
            j.put("max10NMSpeedTime1", movingAvgMaxes[2].getMaxSpeedT1());
            return j;
        }
    }

    private class MySample {
        long ts;
        double distance;
        double speed;
        double maxSpeed;
        EngineStatus eng;
        boolean anchor;
        int period;

        MySample(ResultSet rs) throws SQLException {
            ts = rs.getTimestamp(1).getTime();
            distance = rs.getDouble(2);
            speed = rs.getDouble(3);
            maxSpeed = rs.getDouble(4);
            eng = EngineStatus.valueOf(rs.getInt(5));
            anchor = 1 == rs.getInt(6);
            period = rs.getInt(7);
        }
    }

    public Stats run(Instant from, Instant to) throws DBException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(SQL)) {
                st.setTimestamp(1, new Timestamp(from.toEpochMilli()));
                st.setTimestamp(2, new Timestamp(to.toEpochMilli()));
                Stats stats = new Stats();
                MySample prevSample = null;
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        stats.samples++;
                        MySample sample = new MySample(rs);
                        calcMovingAverageSpeed(stats, sample);
                        calcSailAndEngine(stats, sample, prevSample);
                        calcNavAnchorTime(stats, sample, prevSample);
                        calcMaxSpeed(stats, sample);
                        prevSample = sample;
                    }
                    return stats;
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error("Error calculating track analytics", e);
            throw new DBException("Error calculating track analytics", e);
        }
    }

    private void calcMaxSpeed(Stats stats, MySample sample) {
        if (!sample.anchor) {
            if (sample.maxSpeed > stats.maxSpeed) {
                stats.maxSpeed = sample.maxSpeed;
                stats.maxSpeedTime = sample.ts;
            }
            if (sample.speed > stats.maxSampleAverageSpeed) {
                stats.maxSampleAverageSpeed = sample.speed;
                stats.maxSampleAverageSpeedTime = sample.ts;
            }
            double avgSpeed = sample.distance / (sample.period / 60.0 / 60.0);
            if (avgSpeed > stats.max30sAverageSpeed) {
                stats.max30sAverageSpeed = avgSpeed;
                stats.max30sAverageSpeedTime = sample.ts;
            }

        }
    }

    private void calcNavAnchorTime(Stats stats, MySample sample, MySample prev) {
        long deltaTime = (prev == null) ? 0 : (sample.ts - prev.ts);
        if (sample.anchor)
            stats.totalAnchorTime += deltaTime;
        else {
            stats.totalNavigationTime += deltaTime;
            stats.totalNavigationDistance += sample.distance;
        }
    }

    private void calcSailAndEngine(Stats stats, MySample sample, MySample prev) {
        stats.sailEngineDistance[sample.eng.getValue()] += sample.distance;
        stats.sailEngineTime[sample.eng.getValue()] += (prev == null) ? 0 : (sample.ts - prev.ts);
    }

    private void calcMovingAverageSpeed(Stats stats, MySample sample) {
        Pair<Long, Double> p = new Pair<>(sample.ts, sample.distance);
        for (MovingAverageMax m : stats.movingAvgMaxes) m.addSample(p);
    }
}
