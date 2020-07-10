package com.aboni.nmea.router.services;

import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.GPSStatus;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.impl.NMEAGPSStatusAgent;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GPSStatusService extends JSONWebService {

    private GPSStatus statusProvider;

    @Inject
    public GPSStatusService(@NotNull NMEARouter router) {
        for (String ag_id : router.getAgents()) {
            NMEAAgent ag = router.getAgent(ag_id);
            if (ag instanceof GPSStatus) {
                statusProvider = (GPSStatus) ag;
                break;
            }
        }
        setLoader(config -> {
            if (statusProvider != null) {
                JSONObject res = new JSONObject();

                List<JSONObject> l = new ArrayList<>();
                for (NMEAGPSStatusAgent.SatInfo s : statusProvider.getSatellites()) {
                    JSONObject jSat = new JSONObject();
                    jSat.put("id", s.getId());
                    jSat.put("elevation", s.getElevation());
                    jSat.put("azimuth", s.getAzimuth());
                    jSat.put("noise", s.getNoise());
                    jSat.put("used", s.isUsed());
                    l.add(jSat);
                }
                res.put("satsList", new JSONArray(l));

                GeoPositionT p = statusProvider.getPosition();
                if (p != null) {
                    res.put("latitude", Utils.formatLatitude(p.getLatitude()));
                    res.put("longitude", Utils.formatLongitude(p.getLongitude()));
                    res.put("timestamp", DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochMilli(p.getTimestamp())));
                }

                if (!Double.isNaN(statusProvider.getCOG())) res.put("COG", statusProvider.getCOG());
                if (!Double.isNaN(statusProvider.getSOG())) res.put("SOG", statusProvider.getSOG());
                if (!Double.isNaN(statusProvider.getHDOP())) res.put("HDOP", statusProvider.getHDOP());

                res.put("fix", statusProvider.getGPSFix().toString());

                res.put("anchor", statusProvider.isAnchor(System.currentTimeMillis()));

                return res;
            }
            return null;
        });
    }
}
