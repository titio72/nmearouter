package com.aboni.nmea.router.n2k;

import com.aboni.misc.Utils;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CANBOATDecoderImpl implements CANBOATDecoder {

    private interface Converter {
        Sentence getSentence(JSONObject fields);
    }

    private final Map<Integer, Converter> converterMap;
    private JSONObject lastHeading = null;
    private JSONObject lastSOGCOG = null;
    private Instant lastTime;
    private long lastLocalTime;

    @Inject
    public CANBOATDecoderImpl() {
        converterMap = new HashMap<>();
        converterMap.put(120306, this::handleWind);
        converterMap.put(130306, this::handleWind);
        converterMap.put(128267, this::handleDepth);
        converterMap.put(128259, this::handleSpeed);
        converterMap.put(130310, this::handleWaterTemp);
        converterMap.put(127250, this::handleHeading);
        converterMap.put(129026, this::handleSOGCOG);
        converterMap.put(129025, this::handlePosition);
        converterMap.put(126992, this::handleSystemTime);
        converterMap.put(127245, this::handleRudder);
        converterMap.put(127257, this::handleAttitude);
        converterMap.put(127251, this::handleRateOfTurn);
    }

    @Override
    public Sentence getSentence(JSONObject canBoatSentence) {
        int pgn = canBoatSentence.getInt("pgn");
        JSONObject fields = canBoatSentence.getJSONObject("fields");
        Converter c = converterMap.getOrDefault(pgn, (JSONObject f) -> null);
        return c.getSentence(fields);
    }

    @Override
    public Sentence getSentence(int pgn, JSONObject fields) {
        Converter c = converterMap.getOrDefault(pgn, (JSONObject f) -> null);
        return c.getSentence(fields);
    }

    private Sentence handleRateOfTurn(JSONObject fields) {
        double r = fields.getDouble("Rate");
        ROTSentence rot = (ROTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.ROT);
        rot.setStatus(DataStatus.ACTIVE);
        rot.setRateOfTurn(r);
        return rot;
    }

    private Sentence handleAttitude(JSONObject fields) {
        double roll = Utils.round(fields.getDouble("Roll") - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), 1);
        double pitch = Utils.round(fields.getDouble("Pitch") - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0), 1);
        double yaw = fields.getDouble("Yaw");
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        xdr.addMeasurement(new Measurement("A", yaw, "D", "YAW"));
        xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
        xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
        return xdr;
    }

    private Sentence handleRudder(JSONObject fields) {
        if (fields.getInt("Instance") == 0) {
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            double angle = fields.getDouble("Position");
            rsa.setRudderAngle(Side.STARBOARD, Utils.normalizeDegrees180To180(angle));
            return rsa;
        } else {
            return null;
        }

    }

    private Sentence handleSystemTime(JSONObject fields) {
        String sDate = fields.getString("Date").replace(".", "-");
        String sTime = fields.getString("Time");
        lastTime = Instant.parse(sDate + "T" + sTime + "Z");
        lastLocalTime = System.currentTimeMillis();
        return null;
    }

    private Sentence handlePosition(JSONObject fields) {
        RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RMC);
        rmc.setPosition(new Position(fields.getDouble("Latitude"), fields.getDouble("Longitude")));
        rmc.setVariation(0.0);
        rmc.setDirectionOfVariation(CompassPoint.EAST);
        rmc.setMode(FaaMode.AUTOMATIC);
        rmc.setStatus(DataStatus.ACTIVE);
        if (lastSOGCOG != null) {
            try {
                rmc.setCourse(lastSOGCOG.getDouble("COG"));
            } catch (JSONException ignored) {
            }
            rmc.setSpeed(lastSOGCOG.getDouble("SOG"));
        }
        if (lastTime != null) {
            Instant ts = lastTime.minusMillis(System.currentTimeMillis() - lastLocalTime);
            DateTimeFormatter fmDate = DateTimeFormatter.ofPattern("ddMMYY").withZone(ZoneId.of("UTC"));
            DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("HHmmss").withZone(ZoneId.of("UTC"));
            rmc.setDate(new Date(fmDate.format(ts)));
            rmc.setTime(new Time(fmTime.format(ts)));
        }
        return rmc;
    }

    private Sentence handleSOGCOG(JSONObject fields) {
        lastSOGCOG = fields;
        return null;
    }

    private Sentence handleWaterTemp(JSONObject fields) {
        MTWSentence mtw = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
        mtw.setTemperature(fields.getDouble("Water Temperature"));
        return mtw;
    }

    private Sentence handleHeading(JSONObject fields) {
        double heading = fields.getDouble("Heading");
        String ref = fields.getString("Reference");
        switch (ref) {
            case "Magnetic":
                lastHeading = fields;
                HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                hdm.setHeading(heading);
                return hdm;
            case "True":
                HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
                hdt.setHeading(heading);
                return hdt;
            default:
                return null;
        }
    }

    private Sentence handleSpeed(JSONObject fields) {
        VHWSentence vhw = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
        if (lastHeading != null) {
            vhw.setMagneticHeading(lastHeading.getDouble("Heading"));
        }
        double speed = fields.getDouble("Speed Water Referenced");
        vhw.setSpeedKnots(speed);
        vhw.setSpeedKmh(speed * 1.852);
        return vhw;
    }

    private Sentence handleDepth(JSONObject fields) {
        double depth = fields.getDouble("Depth");
        double offset = fields.getDouble("Offset");
        DPTSentence dpt = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
        dpt.setOffset(offset);
        dpt.setDepth(depth);
        return dpt;
    }

    private Sentence handleWind(JSONObject jsonObject) {
        // mind that speed in m/s
        double windSpeed = jsonObject.getDouble("Wind Speed") * 1.94384;
        double windAngle = jsonObject.getDouble("Wind Angle");
        String ref = jsonObject.getString("Reference");
        MWVSentence mwv = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        mwv.setAngle(windAngle);
        mwv.setSpeed(windSpeed);
        mwv.setSpeedUnit(Units.KNOT);
        mwv.setTrue(!"Apparent".equals(ref));
        mwv.setStatus(DataStatus.ACTIVE);
        return mwv;
    }
}
