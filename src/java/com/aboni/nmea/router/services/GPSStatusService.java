package com.aboni.nmea.router.services;

import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.GPSStatus;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.SatInfo;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.utils.Log;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GPSStatusService extends JSONWebService {

    private final Log log;
    private GPSStatus statusProvider;
    private final NMEARouter router;

    private GPSStatus findService() {
        if (statusProvider==null) {
            for (String ag_id : router.getAgents()) {
                NMEAAgent ag = router.getAgent(ag_id);
                if (ag instanceof GPSStatus) {
                    statusProvider = (GPSStatus) ag;
                    break;
                }
            }
        }
        return statusProvider;
    }

    @Inject
    public GPSStatusService(@NotNull NMEARouter router, @NotNull Log log) {
        super(log);
        this.log = log;
        this.router = router;
        setLoader((ServiceConfig config) -> {
            GPSStatus st = findService();
            if (st != null) {
                JSONObject res = new JSONObject();
                List<JSONObject> l = new ArrayList<>();
                for (SatInfo s : st.getSatellites()) {
                    l.add(getJsonSat(s));
                }
                res.put("satsList", new JSONArray(l));

                GeoPositionT p = st.getPosition();
                if (p != null) {
                    res.put("latitude", Utils.formatLatitude(p.getLatitude()));
                    res.put("longitude", Utils.formatLongitude(p.getLongitude()));
                }

                Instant time = st.getPositionTime();
                if (time != null) {
                    res.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(time));
                }
                setDoubleValue(res, st.getCOG(), "COG");
                setDoubleValue(res, st.getSOG(), "SOG");
                setDoubleValue(res, st.getHDOP(), "HDOP");
                res.put("fix", st.getGPSFix());
                return res;
            }
            return null;
        });
    }

    private void setDoubleValue(JSONObject res, double v, String attribute) {
        if (!Double.isNaN(v)) res.put(attribute, v);
    }

    @Nonnull
    private JSONObject getJsonSat(SatInfo s) {
        JSONObject jSat = new JSONObject();
        jSat.put("id", s.getId());
        jSat.put("elevation", s.getElevation());
        jSat.put("azimuth", s.getAzimuth());
        jSat.put("noise", s.getNoise());
        jSat.put("used", s.isUsed());
        if (s.getSat() != null) {
            jSat.put("name", s.getSat().getName());
            jSat.put("clock", s.getSat().getClock());
            jSat.put("orbit", s.getSat().getOrbit());
            jSat.put("signal", s.getSat().getSignal());
            jSat.put("date", s.getSat().getDate());
        }
        return jSat;
    }
}
