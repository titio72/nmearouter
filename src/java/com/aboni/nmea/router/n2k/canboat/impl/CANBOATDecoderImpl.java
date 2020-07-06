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
import com.aboni.nmea.router.n2k.PGNDefParseException;
import com.aboni.nmea.router.n2k.PGNs;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CANBOATDecoderImpl implements CANBOATDecoder {

    private interface Converter {
        Sentence[] getSentence(JSONObject fields);
    }

    private final Map<Integer, Converter> converterMap;
    private JSONObject lastHeading = null;
    private JSONObject lastSOGCOG = null;
    private Instant lastTime;
    private long lastLocalTime;
    private PGNs pgns;

    @Inject
    public CANBOATDecoderImpl() {
        try {
            pgns = new PGNs("conf/pgns.json", null);
        } catch (PGNDefParseException e) {
            ServerLog.getLogger().errorForceStacktrace("CANBOATDecoder Cannont load pgn definitions", e);
        }
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
        converterMap.put(130311, this::handleEnvironment);
        converterMap.put(126996, this::handleDeviceInfo);
    }

    private Sentence[] handleDeviceInfo(JSONObject jsonObject) {
        String r = "Device ";
        if (jsonObject.has("Model ID")) r += "Model {" + jsonObject.getString("Model ID") + "}";
        if (jsonObject.has("Software Version Code")) r += " Software ver. {" + jsonObject.getString("Software Version Code") + "}";
        if (jsonObject.has("Model Version")) r += " Version {" + jsonObject.getString("Model Version") + "}";
        if (jsonObject.has("Model Serial Code")) r += " Serial {" + jsonObject.getString("Model Serial Code") + "}";
        if (jsonObject.has("Certification Level")) r += " Cert. Level {" + jsonObject.getInt("Certification Level") + "}";
        if (jsonObject.has("NMEA 2000 Version")) r += " NMEA2K Ver. {" + jsonObject.getInt("NMEA 2000 Version") + "}";
        if (jsonObject.has("Product Code")) r += " Product Code {" + jsonObject.getInt("Product Code") + "}";

        System.out.println(r);

        return TEMPLATE;
    }

    @Override
    public Sentence[] getSentence(JSONObject canBoatSentence) {
        int pgn = canBoatSentence.getInt("pgn");
        JSONObject fields = canBoatSentence.getJSONObject("fields");
        Converter c = converterMap.getOrDefault(pgn, (JSONObject f) -> null);
        try {
            return c.getSentence(fields);
        } catch (JSONException e) {
            ServerLog.getLogger().error(String.format("CANBOAT Decoder error {%s}", canBoatSentence), e);
            return TEMPLATE;
        }
    }

    @Override
    public Sentence[] getSentence(int pgn, JSONObject fields) {
        Converter c = converterMap.getOrDefault(pgn, (JSONObject f) -> null);
        return c.getSentence(fields);
    }

    private static final Sentence[] TEMPLATE = new Sentence[]{};

    private Sentence[] handleEnvironment(JSONObject jsonObject) {
        // "SID":0,
        // "Temperature Source":"Main Cabin Temperature",
        // "Humidity Source":"Inside",
        // "Temperature":26.00,
        // "Humidity":46.000,
        // "Atmospheric Pressure":101900
        List<Sentence> res = new ArrayList<>();
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (jsonObject.has("Temperature Source")
                && "Main Cabin Temperature".equals(jsonObject.get("Temperature Source"))
                && jsonObject.has("Temperature")) {
            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTA);
            double t = jsonObject.getDouble("Temperature");
            mta.setTemperature(t);
            xdr.addMeasurement(new Measurement("C", Utils.round(t, 1), "C", "CabinTemp"));
            res.add(mta);
            send = true;
        }
        if (jsonObject.has("Humidity Source")
                && jsonObject.has("Humidity")) {
            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MHU);
            double h = jsonObject.getDouble("Humidity");
            mhu.setRelativeHumidity(h);
            xdr.addMeasurement(new Measurement("P", Utils.round(h, 2), "H", "Humidity"));
            res.add(mhu);
            send = true;
        }
        if (jsonObject.has("Atmospheric Pressure")) {
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MMB);
            double p = jsonObject.getDouble("Atmospheric Pressure") / 100.0; // millibar
            mmb.setBars(p / 1000.0);
            mmb.setInchesOfMercury(Math.round(p * 760));
            xdr.addMeasurement(new Measurement("B", p, "B", "Barometer"));
            res.add(mmb);
            send = true;
        }
        if (send) {
            res.add(xdr);
        }
        return res.toArray(TEMPLATE);
    }

    private Sentence[] handleRateOfTurn(JSONObject fields) {
        if (fields.has("Rate")) {
            double r = fields.getDouble("Rate");
            ROTSentence rot = (ROTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.ROT);
            rot.setStatus(DataStatus.ACTIVE);
            rot.setRateOfTurn(r);
            return new Sentence[]{rot};
        } else {
            return TEMPLATE;
        }
    }

    private Sentence[] handleAttitude(JSONObject fields) {
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (fields.has("Yaw")) {
            double yaw = fields.getDouble("Yaw");
            xdr.addMeasurement(new Measurement("A", yaw, "D", "YAW"));
            send = true;
        }
        if (fields.has("Roll")) {
            double roll = Utils.round(fields.getDouble("Roll") - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), 1);
            xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
            send = true;
        }
        if (fields.has("Pitch")) {
            double pitch = Utils.round(fields.getDouble("Pitch") - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0), 1);
            xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
            send = true;
        }
        if (send)
            return new Sentence[]{xdr};
        else
            return new Sentence[]{};
    }

    private Sentence[] handleRudder(JSONObject fields) {
        if (fields.getInt("Instance") == 0) {
            if (fields.has("Position")) {
                double angle = fields.getDouble("Position");
                RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);

                rsa.setRudderAngle(Side.STARBOARD, Utils.normalizeDegrees180To180(angle));
                rsa.setStatus(Side.STARBOARD, DataStatus.ACTIVE);
                return new Sentence[]{rsa};
            }
        }
        return TEMPLATE;
    }

    private Sentence[] handleSystemTime(JSONObject fields) {
        String sDate = fields.getString("Date").replace(".", "-");
        String sTime = fields.getString("Time");
        lastTime = Instant.parse(sDate + "T" + sTime + "Z");
        lastLocalTime = System.currentTimeMillis();

        ZDASentence zda = (ZDASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.ZDA);
        zda.setTimeAndLocalZone(new Time(Integer.parseInt(sTime.substring(0, 2)),
                Integer.parseInt(sTime.substring(3, 5)),
                Integer.parseInt(sTime.substring(6, 8)), 0, 0));
        zda.setDate(new Date(Integer.parseInt(sDate.substring(0, 4)),
                Integer.parseInt(sDate.substring(5, 7)),
                Integer.parseInt(sDate.substring(8, 10))));
        return new Sentence[]{zda};
    }

    private Sentence[] handlePosition(JSONObject fields) {
        RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RMC);
        rmc.setPosition(new Position(fields.getDouble("Latitude"), fields.getDouble("Longitude")));
        rmc.setVariation(0.0);
        rmc.setDirectionOfVariation(CompassPoint.EAST);
        rmc.setMode(FaaMode.AUTOMATIC);
        rmc.setStatus(DataStatus.ACTIVE);
        if (lastSOGCOG != null) {
            try {
                rmc.setCourse(lastSOGCOG.getDouble("COG"));
                rmc.setSpeed(lastSOGCOG.getDouble("SOG"));
            } catch (JSONException ignored) {
                // do nothing
            }
        }
        if (lastTime != null) {
            Instant ts = lastTime.minusMillis(System.currentTimeMillis() - lastLocalTime);
            DateTimeFormatter fmDate = DateTimeFormatter.ofPattern("ddMMYY").withZone(ZoneId.of("UTC"));
            DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("HHmmss").withZone(ZoneId.of("UTC"));
            rmc.setDate(new Date(fmDate.format(ts)));
            rmc.setTime(new Time(fmTime.format(ts)));
        }
        return new Sentence[]{rmc};
    }

    private Sentence[] handleSOGCOG(JSONObject fields) {
        lastSOGCOG = fields;
        return TEMPLATE;
    }

    private Sentence[] handleWaterTemp(JSONObject fields) {
        MTWSentence mtw = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
        mtw.setTemperature(fields.getDouble("Water Temperature"));
        return new Sentence[]{mtw};
    }

    private Sentence[] handleHeading(JSONObject fields) {
        double heading = fields.getDouble("Heading");
        String ref = fields.getString("Reference");
        switch (ref) {
            case "Magnetic":
                lastHeading = fields;
                HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                hdm.setHeading(heading);
                return new Sentence[]{hdm};
            case "True":
                HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
                hdt.setHeading(heading);
                return new Sentence[]{hdt};
            default:
                return TEMPLATE;
        }
    }

    private Sentence[] handleSpeed(JSONObject fields) {
        VHWSentence vhw = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
        if (lastHeading != null) {
            vhw.setMagneticHeading(lastHeading.getDouble("Heading"));
        }
        double speed = fields.getDouble("Speed Water Referenced");
        vhw.setSpeedKnots(speed);
        vhw.setSpeedKmh(speed * 1.852);
        return new Sentence[]{vhw};
    }

    private Sentence[] handleDepth(JSONObject fields) {
        double depth = fields.getDouble("Depth");
        double offset = fields.getDouble("Offset");
        DPTSentence dpt = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
        dpt.setOffset(offset);
        dpt.setDepth(depth);
        return new Sentence[]{dpt};
    }

    private Sentence[] handleWind(JSONObject jsonObject) {
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
        return new Sentence[]{mwv};
    }

    private Sentence[] handleAISClassBReport(JSONObject jsonObject) {
        // 129039 AIS Class B Position Report {"User ID":247324130,"Unit type":"CS","Can handle Msg 22":"Yes","Regional Application":0,"AIS communication state":"ITDMA","Latitude":43.0578155,"SOG":0.05,"Band":"entire marine band","Integrated Display":"Yes","Longitude":9.8365983,"Repeat Indicator":"Initial","Regional Application 1":0,"Time Stamp":"26","AIS mode":"Assigned","AIS Transceiver information":"Channel A VDL reception","RAIM":"in use","DSC":"Yes","Communication State":"3","Position Accuracy":"High","COG":315.1,"Message ID":18}
        long mmsi = jsonObject.getLong("User ID");
        return TEMPLATE;
    }


}
