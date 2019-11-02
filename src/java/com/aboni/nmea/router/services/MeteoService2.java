package com.aboni.nmea.router.services;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class MeteoService2 extends JSONWebService {

    private static final String SQL = "select type, TS, vMin, v, vMax from meteo where TS>=? and TS<?";

    public MeteoService2() {
        super();
        setLoader(this::getResult);
    }

    private JSONObject getResult(ServiceConfig config) {
        Calendar from = config.getParamAsDate("dateFrom", 0);
        Calendar to = config.getParamAsDate("dateTo", 1);

        JSONObject res = new JSONObject();
        if (from.before(to)) {
            try (DBHelper db = new DBHelper(true)) {
                try (PreparedStatement st = db.getConnection().prepareStatement(SQL)) {
                    st.setTimestamp(1, new Timestamp(from.getTimeInMillis()));
                    st.setTimestamp(2, new Timestamp(to.getTimeInMillis()));
                    if (st.execute()) {
                        try (ResultSet rs = st.getResultSet()) {
                            while (rs.next()) {
                                String type = rs.getString(1);
                                Timestamp ts = rs.getTimestamp(2);
                                double vMin = rs.getDouble(3);
                                double vAvg = rs.getDouble(4);
                                double vMax = rs.getDouble(5);
                                addSample(type, ts.getTime(), vMin, vAvg, vMax, res);
                            }
                        }
                        return res;
                    }
                }
                return getErrorJsonObject("Something went wrong during collection of meteo time series");
            } catch (SQLException | ClassNotFoundException e) {
                return getErrorJsonObject("Error loading meteo time series - check server logs");
            }
        } else {
            return getErrorJsonObject("Error loading meteo time series - dates inverted");
        }
    }

    private void addSample(String type, long t, double vMin, double v, double vMax, JSONObject res) {
        JSONArray a;
        if (res.has(type)) {
            a = res.getJSONArray(type);
        } else {
            a = new JSONArray();
            res.put(type, a);
        }
        JSONObject s = new JSONObject();
        s.put("time", t);
        s.put("vMin", vMin);
        s.put("v", v);
        s.put("vMax", vMax);
        a.put(s);
    }

    private JSONObject getErrorJsonObject(String msg) {
        ServerLog.getLogger().error(msg);
        JSONObject resErr = new JSONObject();
        resErr.put("Error", msg);
        return resErr;
    }
}
