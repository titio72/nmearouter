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

package com.aboni.toolkit;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.data.*;
import com.aboni.nmea.router.data.impl.TimerFilterFixed;
import com.aboni.nmea.router.data.metrics.Metrics;
import com.aboni.nmea.router.message.Message;
import com.aboni.utils.Log;
import com.aboni.utils.LogAdmin;
import com.aboni.utils.ThingsFactory;
import com.aboni.utils.db.DBHelper;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UpdateMeteo {

    private static class MyTimeProvider extends TimestampProvider {

        long time = 0;

        @Override
        public long getNow() {
            return time;
        }
    }

    private static final MyTimeProvider timeProvider = new MyTimeProvider();

    private static void load(Sampler sampler) {
        try (DBHelper db = new DBHelper(false)) {
            try (PreparedStatement st = db.getConnection().prepareStatement(
                    "select TS, v, type, id from meteo where " +
                            "TS>='2022-07-22 00:00:00' and " +
                            "TS<='2022-07-29 07:52:31'")) {

                long firstId = 0, lastId = 0;
                if (st.execute()) {
                    try (ResultSet rs = st.getResultSet()) {
                        long t0 = 0L;
                        while (rs.next()) {
                            DBRecordRouterMessage m = new DBRecordRouterMessage(rs);
                            //System.out.println(m.getTimestamp() + " " + m.m.type);
                            timeProvider.time = m.getTimestamp();
                            if (t0==0) {
                                t0 = m.getTimestamp();
                                firstId = m.m.id;
                            }
                            else if (Utils.isNotNewerThan(t0, m.getTimestamp(), 60000)) {
                                sampler.dumpAndReset();
                                t0 = m.getTimestamp();
                            }
                            sampler.onSentence(m);
                            lastId = m.m.id;
                        }
                        sampler.dumpAndReset(true);
                        System.out.println(firstId + " " + lastId);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static final class DBRecordRouterMessage implements RouterMessage {

        DBRecordMessage m;

        DBRecordRouterMessage(ResultSet rs) throws SQLException {
            m = new DBRecordMessage(rs);
        }

        @Override
        public long getTimestamp() {
            return m.ts.getTime();
        }

        @Override
        public String getSource() {
            return "MMM";
        }

        @Override
        public Object getPayload() {
            return m;
        }

        @Override
        public Message getMessage() {
            return m;
        }

        @Override
        public JSONObject getJSON() {
            return null;
        }
    }


    static final class DBRecordMessage implements Message {

        private final String type;
        private final double v;
        private final Timestamp ts;
        private final long id;

        private DBRecordMessage(ResultSet rs) throws SQLException {
            v = rs.getDouble(2);
            type = rs.getString(3);
            ts = rs.getTimestamp(1);
            id = rs.getLong(4);
        }

        @Override
        public String getMessageContentType() {
            return "DB REC";
        }
    }

    private final static long ONE_MINUTE = 60000L;

    public static void main(String[] args) {
        FileWriter w;
        timeProvider.setSkew(0, 100);
        try {
            w = new FileWriter("meteo.csv");
            Injector injector = Guice.createInjector(new NMEARouterModule());
            ThingsFactory.setInjector(injector);
            Log log = ThingsFactory.getInstance(LogAdmin.class);
            ConfJSON cJ;
            try {
                cJ = new ConfJSON();
                cJ.getLogLevel();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Sampler meteoSampler = new Sampler(log, timeProvider, new StatsWriter() {
                @Override
                public void write(StatsSample s, long ts) {
                    try {
                        String ss = String.format("%s,%dl,%.3f,%.3f,%.3f\n", s.getTag(), s.getT1(), s.getMin(), s.getAvg(), s.getMax());
                        w.write(ss);
                        System.out.print(ss);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void init() {}

                @Override
                public void dispose() {}
            }, "UpdateMeteoDB");


            meteoSampler.initMetric(Metrics.PRESSURE,
                    (Message m) -> "PR_".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(5 * ONE_MINUTE, 500),
                    "PR_", 800.0, 1100.0);
            meteoSampler.initMetric(Metrics.WATER_TEMPERATURE,
                    (Message m) -> "WT_".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(10 * ONE_MINUTE, 500),
                    "WT_", -20.0, 60.0);
            meteoSampler.initMetric(Metrics.AIR_TEMPERATURE,
                    (Message m) -> "AT0".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(10 * ONE_MINUTE, 500),
                    "AT0", -20.0, 60.0);
            meteoSampler.initMetric(Metrics.HUMIDITY,
                    (Message m) -> "HUM".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(10 * ONE_MINUTE, 500),
                    "HUM", 0.0, 150.0);
            meteoSampler.initMetric(Metrics.WIND_SPEED,
                    (Message m) -> "TW_".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(ONE_MINUTE, 500),
                    "TW_", 0.0, 100.0);
            meteoSampler.initMetric(Metrics.WIND_DIRECTION,
                    (Message m) -> "TWD".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(ONE_MINUTE, 500),
                    "TWD", -360.0, 360.0);
            meteoSampler.initMetric(Metrics.ROLL,
                    (Message m) -> "ROL".equals(((DBRecordMessage) m).type),
                    (Message m) -> ((DBRecordMessage) m).v,
                    new TimerFilterFixed(ONE_MINUTE, 500),
                    "ROL", -180.0, 180.0);

            meteoSampler.start();
            load(meteoSampler);
            w.close();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }
}
