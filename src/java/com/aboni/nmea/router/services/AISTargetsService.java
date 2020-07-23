package com.aboni.nmea.router.services;

import com.aboni.geo.Course;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.AISTargets;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouter;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.n2k.AISStaticData;
import com.aboni.nmea.router.n2k.impl.AISPositionReport;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class AISTargetsService extends JSONWebService {

    private AISTargets targetsProvider;
    private NMEACache cache;

    @Inject
    public AISTargetsService(@NotNull NMEARouter router, NMEACache cache) {
        this.cache = cache;
        for (String ag_id : router.getAgents()) {
            NMEAAgent ag = router.getAgent(ag_id);
            if (ag instanceof AISTargets) {
                targetsProvider = (AISTargets) ag;
                break;
            }
        }
        setLoader((ServiceConfig config) -> {
            if (targetsProvider != null) {
                List<AISPositionReport> reports = targetsProvider.getAISTargets();
                JSONObject res = new JSONObject();
                List<JSONObject> l = new ArrayList<>();
                if (reports != null) {
                    Position myPos = null;
                    if (cache != null && cache.getLastPosition() != null && cache.getLastPosition().getData() != null && cache.getLastPosition().getData().getPosition() != null) {
                        myPos = cache.getLastPosition().getData().getPosition();
                    } else {
                        myPos = new Position(43.25678, 10.071234);
                    }
                    for (AISPositionReport r : reports) {
                        JSONObject j = new JSONObject();
                        j.put("MMSI", r.getMMSI());
                        j.put("class", r.getAISClass());
                        j.put("latitude", r.getPosition().getLatitude());
                        j.put("longitude", r.getPosition().getLongitude());
                        j.put("s_latitude", Utils.formatLatitude(r.getPosition().getLatitude()));
                        j.put("s_longitude", Utils.formatLongitude(r.getPosition().getLongitude()));
                        if (r.getPositionAccuracy() != null) j.put("accuracy", r.getPositionAccuracy());
                        if (!Double.isNaN(r.getSog())) j.put("SOG", r.getSog());
                        if (!Double.isNaN(r.getCog())) j.put("COG", r.getCog());
                        if (!Double.isNaN(r.getHeading())) j.put("heading", r.getCog());
                        if (r.getRepeatIndicator() != null) j.put("repeatIndicator", r.getRepeatIndicator());
                        if (r.getTimestampStatus() != null) j.put("timeStampStatus", r.getTimestampStatus());
                        if (r.getTimestamp() != 0xFF) j.put("timestamp", r.getTimestamp());
                        if (myPos != null) {
                            Course c1 = new Course(myPos, r.getPosition());
                            j.put("distance", c1.getDistance());
                            j.put("bearing", c1.getCOG());
                        }
                        if (r.getNavStatus() != null) j.put("status", r.getNavStatus());

                        AISStaticData data = targetsProvider.getData(r.getMMSI());
                        if (data != null) {
                            j.put("name", data.getName());
                            if (data.getTypeOfShip() != null) j.put("vessel_type", data.getTypeOfShip());
                            j.put("length", data.getLength());
                            j.put("beam", data.getBeam());
                            if (data.getCallSign() != null) j.put("callsign", data.getCallSign());
                            if (data.getAisTransceiverInfo() != null) j.put("tranceiver", data.getAisTransceiverInfo());
                        }

                        l.add(j);
                    }
                }
                res.put("targets", new JSONArray(l));
                return res;
            }
            return null;
        });
    }
}
