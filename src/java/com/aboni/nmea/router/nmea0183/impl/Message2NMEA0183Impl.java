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

package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.*;
import com.aboni.nmea.router.nmea0183.Message2NMEA0183;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.utils.HWSettings;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import javax.inject.Inject;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.aboni.nmea.router.message.HumiditySource.INSIDE;

public class Message2NMEA0183Impl implements Message2NMEA0183 {

    private static final Sentence[] TEMPLATE = new Sentence[0];

    private static final DateTimeFormatter fTIME = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter fDATE = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Inject
    public Message2NMEA0183Impl() {
        // do nothing
    }

    @Override
    public Sentence[] convert(Message message) {
        if (message instanceof NMEA0183Message) {
            return new Sentence[]{((NMEA0183Message) message).getSentence()};
        } else if (message instanceof MsgPositionAndVector) {
            return handlePositionAndVector((MsgPositionAndVector) message);
        } else if (message instanceof MsgWindData) {
            return handleWindData((MsgWindData) message); // Wind Data
        } else if (message instanceof MsgWaterDepth) {
            return handleWaterDepth((MsgWaterDepth) message); // Water Depth
        } else if (message instanceof MsgSpeedAndHeading) {
            return handleSpeedHeading((MsgSpeedAndHeading) message); // Speed & Heading
        } else if (message instanceof MsgHeading) {
            return handleHeading((MsgHeading) message); // Vessel Heading
        } else if (message instanceof MsgSatellites) {
            return handleSatellites((MsgSatellites) message); // Sats to GSV
        } else if (message instanceof MsgSystemTime) {
            return handleSystemTime((MsgSystemTime) message); // System time
        } else if (message instanceof MsgAttitude) {
            return handleAttitude((MsgAttitude) message); // Attitude)
        } else if (message instanceof MsgRudder) {
            return handleRudder((MsgRudder) message); // Rudder
        } else if (message instanceof MsgRateOfTurn) {
            return handleRateOfTurn((MsgRateOfTurn) message); // Rate of turn
        } else if (message instanceof MsgTemperature) {
            return handleTemperature((MsgTemperature) message);
        } else if (message instanceof MsgPressure) {
            return handlePressure((MsgPressure) message);
        } else if (message instanceof MsgHumidity) {
            return handleHumidity((MsgHumidity) message);
        } else if (message instanceof MsgBattery) {
            return handleBattery((MsgBattery) message);
        }
        return TEMPLATE;
    }

    private Sentence[] handleBattery(MsgBattery message) {
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (!Double.isNaN(message.getVoltage())) {
            double voltage = message.getVoltage();
            xdr.addMeasurement(new Measurement("V", voltage, "V", "VOLTAGE" + message.getInstance()));
            send = true;
        }
        if (!Double.isNaN(message.getCurrent())) {
            double current = message.getCurrent();
            xdr.addMeasurement(new Measurement("V", current, "A", "CURRENT" + message.getInstance()));
            send = true;
        }
        if (send)
            return new Sentence[]{xdr};
        else
            return TEMPLATE;
    }

    private static Sentence[] handlePositionAndVector(MsgPositionAndVector message) {
        if (message.getPosition() != null && message.getTimestamp() != null
                && !Double.isNaN(message.getSOG()) && !Double.isNaN(message.getCOG())) {
            RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RMC);
            rmc.setPosition(message.getPosition());
            rmc.setVariation(0.0);
            rmc.setDirectionOfVariation(CompassPoint.EAST);
            rmc.setMode(FaaMode.AUTOMATIC);
            rmc.setStatus(DataStatus.ACTIVE);
            rmc.setCourse(message.getCOG());
            rmc.setSpeed(message.getSOG());
            Time t = new Time(message.getTimestamp().atZone(ZoneId.of("UTC")).format(fTIME));
            Date d = new Date(message.getTimestamp().atZone(ZoneId.of("UTC")).format(fDATE));
            rmc.setTime(t);
            rmc.setDate(d);
            return new Sentence[]{rmc};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleSatellites(MsgSatellites message) {
        List<Sentence> res = new ArrayList<>();
        int nSat = message.getNumberOfSats();
        int nGroups = nSat / 12;
        nGroups = (nGroups * 12) < nSat ? nGroups + 1 : nGroups;
        int satIx = 0;
        List<Satellite> satsList = message.getSatellites();
        List<String> satInUse = new ArrayList<>();
        int group = 0;
        while (group < nGroups) {
            satIx = handleSatellitesGroup(res, nSat, satIx, satsList, satInUse, group);
            group++;
        }
        GSASentence gsa = (GSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GSA);
        gsa.setMode(FaaMode.AUTOMATIC);
        int sz = Math.min(12, satInUse.size());
        String[] gsaSats = new String[sz];
        for (int i = 0; i < sz; i++) gsaSats[i] = satInUse.get(i);
        gsa.setSatelliteIds(gsaSats);
        gsa.setFixStatus(satInUse.isEmpty() ? GpsFixStatus.GPS_NA : GpsFixStatus.GPS_2D);
        res.add(gsa);
        return res.toArray(TEMPLATE);
    }

    private static int handleSatellitesGroup(List<Sentence> res, int nSat, int satIx, List<Satellite> satsList, List<String> satInUse, int group) {
        int satsInGroup = Math.min(nSat - (group * 12), 12);
        int sentences = satsInGroup / 4;
        sentences = (sentences * 4) < satsInGroup ? sentences + 1 : sentences;
        int satIxGroup = 0;
        for (int i = 0; i < sentences; i++) {
            GSVSentence s = (GSVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GSV);
            s.setSatelliteCount(satsInGroup);
            s.setSentenceCount(sentences);
            s.setSentenceIndex(i + 1);
            List<SatelliteInfo> l = new ArrayList<>();
            for (int j = 0; j < 4 && satIxGroup < satsInGroup; j++, satIxGroup++, satIx++) {
                Satellite sat = satsList.get(satIx);
                if (sat.getId() != 0xFF) {
                    SatelliteInfo sInfo = new SatelliteInfo(String.format("%02d", sat.getId()), sat.getElevation(), sat.getAzimuth(), sat.getSrn());
                    l.add(sInfo);
                    if ("Used".equals(sat.getStatus())) {
                        satInUse.add(sInfo.getId());
                    }
                }
            }
            s.setSatelliteInfo(l);
            res.add(s);
        }
        return satIx;
    }

    private static Sentence[] handleSystemTime(MsgSystemTime message) {
        if (message.getTime() != null) {
            ZDASentence zda = (ZDASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.ZDA);
            Time t = new Time(message.getTime().atZone(ZoneId.of("UTC")).format(fTIME));
            Date d = new Date(message.getTime().atZone(ZoneId.of("UTC")).format(fDATE));
            zda.setTime(t);
            zda.setDate(d);
            zda.setLocalZoneHours(0);
            zda.setLocalZoneMinutes(0);
            return new Sentence[]{zda};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleSpeedHeading(MsgSpeedAndHeading message) {
        double heading = message.getHeading();
        double speed = message.getSpeedWaterRef();
        DirectionReference ref = message.getReference();
        if (!Double.isNaN(speed) && !Double.isNaN(heading) && ref==DirectionReference.MAGNETIC) {
            VHWSentence vhw = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
            vhw.setMagneticHeading(heading);
            vhw.setSpeedKnots(speed);
            vhw.setSpeedKmh(speed * 1.852);
            return new Sentence[]{vhw};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleHeading(MsgHeading message) {
        double heading = message.getHeading();
        DirectionReference ref = message.getReference();
        if (!Double.isNaN(heading) && ref != null) {
            switch (ref) {
                case MAGNETIC:
                    HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                    hdm.setHeading(heading);
                    return new Sentence[]{hdm};
                case TRUE:
                    HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
                    hdt.setHeading(heading);
                    return new Sentence[]{hdt};
                default:
                    return TEMPLATE;
            }
        }
        return TEMPLATE;
    }

    private static Sentence[] handleWaterDepth(MsgWaterDepth message) {
        double depth = message.getDepth();
        double offset = message.getOffset();
        double range = message.getRange();
        if (!Double.isNaN(depth)) {
            DPTSentence dpt = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
            dpt.setOffset(offset);
            if (!Double.isNaN(offset)) dpt.setDepth(depth);
            if (!Double.isNaN(range)) dpt.setMaximum(range);
            return new Sentence[]{dpt};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleWindData(MsgWindData message) {
        double windSpeed = message.getSpeed();
        double windAngle = message.getAngle();
        boolean apparent = message.isApparent();
        if (!Double.isNaN(windAngle) && !Double.isNaN(windSpeed)) {
            MWVSentence mwv = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
            mwv.setAngle(Utils.normalizeDegrees0To360(windAngle));
            mwv.setSpeed(windSpeed);
            mwv.setSpeedUnit(Units.KNOT);
            mwv.setTrue(!apparent);
            mwv.setStatus(DataStatus.ACTIVE);
            return new Sentence[]{mwv};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleAttitude(MsgAttitude message) {
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (!Double.isNaN(message.getYaw())) {
            double yaw = message.getYaw();
            xdr.addMeasurement(new Measurement("A", yaw, "D", "YAW"));
            send = true;
        }
        if (!Double.isNaN(message.getRoll())) {
            double roll = Utils.round(message.getRoll() - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), 1);
            xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
            send = true;
        }
        if (!Double.isNaN(message.getPitch())) {
            double pitch = Utils.round(message.getPitch() - HWSettings.getPropertyAsDouble("gyro.pitch", 0.0), 1);
            xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
            send = true;
        }
        if (send)
            return new Sentence[]{xdr};
        else
            return TEMPLATE;
    }

    private static Sentence[] handlePressure(MsgPressure message) {
        double pressure = message.getPressure();
        if (PressureSource.ATMOSPHERIC == message.getPressureSource() && !Double.isNaN(pressure)) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MMB);
            mmb.setBars(pressure / 1000.0);
            mmb.setInchesOfMercury(Math.round(pressure * 760));
            xdr.addMeasurement(new Measurement("B", pressure, "B", "Barometer"));
            return new Sentence[]{xdr, mmb};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleHumidity(MsgHumidity message) {
        double humidity = message.getHumidity();
        if (INSIDE == (message.getHumiditySource()) && !Double.isNaN(humidity)) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MHU);
            mhu.setRelativeHumidity(humidity);
            xdr.addMeasurement(new Measurement("P", Utils.round(humidity, 2), "H", "Humidity"));
            return new Sentence[]{xdr, mhu};
        }
        return TEMPLATE;
    }

    private static Sentence[] handleTemperature(MsgTemperature message) {
        double temp = message.getTemperature();
        if (!Double.isNaN(temp)) {
            switch (message.getTemperatureSource()) {
                case MAIN_CABIN_ROOM:
                    XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
                    MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTA);
                    xdr.addMeasurement(new Measurement("C", Utils.round(temp, 1), "C", "CabinTemp"));
                    mta.setTemperature(temp);
                    return new Sentence[]{xdr, mta};
                case SEA:
                    MTWSentence mtw = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
                    mtw.setTemperature(temp);
                    return new Sentence[]{mtw};
                default:
                    return TEMPLATE;
            }
        }
        return TEMPLATE;
    }

    private static Sentence[] handleRudder(MsgRudder message) {
        int instance = message.getInstance();
        double angle = message.getAngle();
        if (instance == 0 && !Double.isNaN(angle)) {
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            rsa.setRudderAngle(Side.STARBOARD, Utils.normalizeDegrees180To180(angle));
            rsa.setStatus(Side.STARBOARD, DataStatus.ACTIVE);
            return new Sentence[]{rsa};
        } else {
            return TEMPLATE;
        }
    }

    private static Sentence[] handleRateOfTurn(MsgRateOfTurn message) {
        double rate = message.getRateOfTurn();
        if (!Double.isNaN(rate)) {
            ROTSentence rot = (ROTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.ROT);
            rot.setStatus(DataStatus.ACTIVE);
            rot.setRateOfTurn(rate);
            return new Sentence[]{rot};
        } else {
            return TEMPLATE;
        }
    }
}
