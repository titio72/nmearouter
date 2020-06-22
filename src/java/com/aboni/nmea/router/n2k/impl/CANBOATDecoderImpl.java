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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.CANBOATDecoder;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.ais.message.AISMessage01;
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
                // do nothing
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

    private Sentence handleAISTargetClassB(JSONObject fields) {
        //"pgn":129810,
        // "description":"AIS Class B static data (msg 24 Part B)",
        // -----------------------------------------------
        // "Message ID":24,
        // "Repeat indicator":
        // "Initial",
        // "User ID":247329560,
        // "Type of ship":"Sailing",
        // "Vendor ID":"f* I",
        // "Callsign":"IN4752",
        // "Length":11.0,
        // "Beam":4.0,
        // "Position reference from Starboard":4.0,
        // "Position reference from Bow":5.0,
        // "Mothership User ID":0,"Spare":0

        //"pgn":129039,
        // "description":"AIS Class B Position Report",
        // -----------------------------------------------
        // "Message ID":18,
        // "Repeat Indicator":"Initial",
        // "User ID":247329560,
        // "Longitude":10.2729654,
        // "Latitude":43.6788368,
        // "Position Accuracy":"High",
        // "RAIM":"in use",
        // "Time Stamp":"25",
        // "SOG":0.00,
        // "Communication State":"0",
        // "AIS Transceiver information":
        // "Channel A VDL reception",
        // "Regional Application":0,
        // "Regional Application":0,
        // "Unit type":"CS",
        // "Integrated Display":"Yes",
        // "DSC":"Yes",
        // "Band":"entire marine band",
        // "Can handle Msg 22":"Yes",
        // "AIS mode":"Assigned",
        // "AIS communication state":"ITDMA"

        AISMessage01 m;


        return null;
    }

    private Sentence handleAISTargetClassA(JSONObject fields) {
        //{"timestamp":"2020-06-13-16:25:03.836","prio":4,"src":0,"dst":255,"pgn":129038,"description":"AIS Class A Position Report",
        // "fields":{"Message ID":1,"User ID":247272700,"Longitude":10.3190336,"Latitude":43.5820159,"Position Accuracy":"Low","RAIM":"not in use","Time Stamp":"2","COG":56.0,"SOG":0.00,"Communication State":"0","AIS Transceiver information":"Channel A VDL reception","Heading":195.0,"Rate of Turn":0.00,"Nav Status":"Under way using engine","AIS Spare":"6"}}
        return null;
    }
}
