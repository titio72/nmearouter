package com.aboni.nmea.router.services;

import com.aboni.geo.Course;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.*;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class AISTargetsService extends JSONWebService {

    private AISTargets targetsProvider;
    private final TimestampProvider timestampProvider;

    @Inject
    public AISTargetsService(@NotNull final NMEARouter router, @NotNull NMEACache cache, @NotNull TimestampProvider tp, @NotNull Log log) {
        super(log);
        this.timestampProvider = tp;
        setLoader((ServiceConfig config) -> {
            findService(router);
            if (targetsProvider != null) {
                log.info(LogStringBuilder.start("WebService").wO("invoke").wV("service", "AISTarget").toString());
                List<AISPositionReport> reports = targetsProvider.getAISTargets();
                JSONObject res = new JSONObject();
                List<JSONObject> targets = getListOfTargets(cache, reports);
                res.put("targets", new JSONArray(targets));
                if (cache.getLastHeading() != null) {
                    double heading = cache.getLastHeading().getData().getHeading();
                    res.put("heading", Utils.round(heading, 1));
                }
                log.info(LogStringBuilder.start("WebService").wO("load").wV("service", "AISTarget").
                        wV("targets", targets.size()).toString());
                return res;
            }
            return null;
        });
    }

    @Nonnull
    private List<JSONObject> getListOfTargets(@NotNull NMEACache cache, List<AISPositionReport> reports) {
        List<JSONObject> l = new ArrayList<>();
        if (reports != null) {
            Position myPos = getCurrentPosition(cache);
            for (AISPositionReport r : reports) {
                JSONObject jTarget = getJsonTarget(myPos, r);
                if (jTarget!=null) l.add(jTarget);
            }
        }
        return l;
    }

    private JSONObject getJsonTarget(Position myPos, AISPositionReport r) {
        if (r.getGPSInfo().getPosition() != null) {
            JSONObject j = new JSONObject();
            j.put("MMSI", r.getMMSI());
            j.put("class", r.getAISClass());
            j.put("latitude", r.getGPSInfo().getPosition().getLatitude());
            j.put("longitude", r.getGPSInfo().getPosition().getLongitude());
            j.put("s_latitude", Utils.formatLatitude(r.getGPSInfo().getPosition().getLatitude()));
            j.put("s_longitude", Utils.formatLongitude(r.getGPSInfo().getPosition().getLongitude()));

            long age = r.getAge(timestampProvider.getNow());
            j.put("age", age);
            j.put("s_age", String.format("%02d:%02d", age / 60000, (age / 1000) % 60));

            setStringAttribute(j, r.getPositionAccuracy(), "accuracy");
            setDoubleAttribute(j, r.getGPSInfo().getSOG(), "SOG");
            setDoubleAttribute(j, r.getGPSInfo().getCOG(), "COG");
            setDoubleAttribute(j, r.getHeading(), "heading");
            setStringAttribute(j, r.getNavStatus(), "status");
            setStringAttribute(j, r.getRepeatIndicator(), "repeatIndicator");
            setStringAttribute(j, r.getTimestampStatus(), "timeStampStatus");
            if (r.getTimestamp() != 0xFF) j.put("timestamp", r.getTimestamp());
            if (myPos != null) {
                Course c1 = new Course(myPos, r.getGPSInfo().getPosition());
                j.put("distance", c1.getDistance());
                j.put("bearing", c1.getCOG());
            }

            AISStaticData data = targetsProvider.getData(r.getMMSI());
            if (data != null) {
                setStringAttribute(j, data.getName(), "name");
                setStringAttribute(j, data.getTypeOfShip(), "vessel_type");
                setDoubleAttribute(j, data.getLength(), "length");
                setDoubleAttribute(j, data.getBeam(), "beam");
                setStringAttribute(j, data.getCallSign(), "callsign");
                setStringAttribute(j, data.getAisTransceiverInfo(), "transceiver");
            }
            return j;
        } else {
            return null;
        }
    }

    private void setDoubleAttribute(JSONObject j, double value, String attribute) {
        if (!Double.isNaN(value)) j.put(attribute, value);
    }

    private void setStringAttribute(JSONObject j, String value, String attribute) {
        if (value != null) j.put(attribute, value);
    }

    private Position getCurrentPosition(NMEACache cache) {
        Position myPos;
        if (cache != null && cache.getLastPosition() != null && cache.getLastPosition().getData() != null && cache.getLastPosition().getData().getPosition() != null) {
            myPos = cache.getLastPosition().getData().getPosition();
        } else {
            myPos = new Position(43.25678, 10.071234);
        }
        return myPos;
    }

    private void findService(@NotNull NMEARouter router) {
        if (targetsProvider==null) {
            for (String ag_id : router.getAgents()) {
                NMEAAgent ag = router.getAgent(ag_id);
                if (ag instanceof AISTargets) {
                    targetsProvider = (AISTargets) ag;
                    break;
                }
            }
        }
    }
}
