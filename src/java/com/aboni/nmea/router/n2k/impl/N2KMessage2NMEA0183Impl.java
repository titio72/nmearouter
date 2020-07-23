package com.aboni.nmea.router.n2k.impl;

import com.aboni.geo.TSAGeoMag;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.utils.HWSettings;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class N2KMessage2NMEA0183Impl implements N2KMessage2NMEA0183 {

    private static final Sentence[] TEMPLATE = new Sentence[0];

    private static final DateTimeFormatter fTIME = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter fDATE = DateTimeFormatter.ofPattern("ddMMyyyy");

    private final TSAGeoMag geo;
    private double lastHeading;
    private long lastHeadingTime = 0;
    private N2KSOGAdCOGRapid lastSOG;
    private N2KGNSSPositionUpdate lastPos;

    public N2KMessage2NMEA0183Impl() {
        geo = new TSAGeoMag(Constants.WMM, ServerLog.getLoggerAdmin().getBaseLogger());
    }

    @Override
    public Sentence[] getSentence(N2KMessage message) {
        if (message != null) {
            switch (message.getHeader().getPgn()) {
                case 130306:
                    return handleWindData((N2KWindData) message); // Wind Data
                case 128267:
                    return handleWaterDepth((N2KWaterDepth) message); // Water Depth
                case 128259:
                    return handleSpeed((N2KSpeed) message); // Speed
                case 127250:
                    return handleHeading((N2KHeading) message); // Vessel Heading
                case 129029:
                    return handlePosition((N2KGNSSPositionUpdate) message); // Position & time
                case 129540:
                    return handleSatellites((N2KSatellites) message); // Sats to GSV
                case 129026:
                    return handleSOGAdCOGRapid((N2KSOGAdCOGRapid) message); // COG & SOG, Rapid Update
                case 126992:
                    return handleSystemTime((N2KSystemTime) message); // System time
                case 127257:
                    return handleAttitude((N2KAttitude) message); // Attitude)
                case 130310:
                    return handleEnvironment310((N2KEnvironment310) message); // Env parameter: Water temp, air temp, pressure
                case 130311:
                    return handleEnvironment311((N2KEnvironment311) message); // Env parameter: temperature, humidity, pressure
                case 127245:
                    return handleRudder((N2KRudder) message); // Rudder
                case 127251:
                    return handleRateOfTurn((N2KRateOfTurn) message); // Rate of turn
                default:
                    return TEMPLATE;
            }
        }
        return TEMPLATE;
    }

    private Sentence[] handleSatellites(N2KSatellites message) {
        List<Sentence> res = new ArrayList<>();
        int nSat = message.getNumberOfSats();
        int nGroups = nSat / 12;
        nGroups = (nGroups * 12) < nSat ? nGroups + 1 : nGroups;
        int satIx = 0;
        List<N2KSatellites.Sat> satsList = message.getSatellites();
        List<String> satInUse = new ArrayList<>();
        for (int group = 0; group < nGroups; group++) {
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
                    N2KSatellites.Sat sat = satsList.get(satIx);
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
        }
        GSASentence gsa = (GSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GSA);
        gsa.setMode(FaaMode.AUTOMATIC);
        gsa.setSatelliteIds(satInUse.toArray(new String[0]));
        gsa.setFixStatus(satInUse.size() > 0 ? GpsFixStatus.GPS_2D : GpsFixStatus.GPS_NA);
        res.add(gsa);
        return res.toArray(TEMPLATE);
    }

    private Sentence[] handleSystemTime(N2KSystemTime message) {
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

    private Sentence[] handleSOGAdCOGRapid(N2KSOGAdCOGRapid message) {
        lastSOG = message;

        List<Sentence> ss = new ArrayList<>();
        double cog = message.getCOG();
        double sog = message.getSOG();

        if (!Double.isNaN(sog) && !Double.isNaN(cog)) {
            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VTG);
            vtg.setTrueCourse(cog);
            if (lastPos != null && lastPos.getPosition() != null)
                vtg.setMagneticCourse(geo.getDeclination(lastPos.getPosition().getLatitude(), lastPos.getPosition().getLongitude()) + cog);
            vtg.setSpeedKnots(sog);
            vtg.setSpeedKmh(sog * 1.852);
            ss.add(vtg);
        }
        Collections.addAll(ss, handleRMC());
        return ss.toArray(TEMPLATE);
    }

    private Sentence[] handleRMC() {
        if (lastSOG != null && lastPos != null && lastSOG.getSID() == lastPos.getSID()) {
            Position p = lastPos.getPosition();
            if (p != null) {
                RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RMC);
                rmc.setPosition(p);
                rmc.setVariation(0.0);
                rmc.setDirectionOfVariation(CompassPoint.EAST);
                rmc.setMode(FaaMode.AUTOMATIC);
                rmc.setStatus(DataStatus.ACTIVE);
                if (!Double.isNaN(lastSOG.getCOG())) rmc.setCourse(lastSOG.getCOG());
                if (!Double.isNaN(lastSOG.getSOG())) rmc.setSpeed(lastSOG.getSOG());
                if (lastPos.getTimestamp() != null) {
                    Time t = new Time(lastPos.getTimestamp().atZone(ZoneId.of("UTC")).format(fTIME));
                    Date d = new Date(lastPos.getTimestamp().atZone(ZoneId.of("UTC")).format(fDATE));
                    rmc.setTime(t);
                    rmc.setDate(d);
                }
                return new Sentence[]{rmc};
            }
        }
        return TEMPLATE;
    }

    private Sentence[] handlePosition(N2KGNSSPositionUpdate message) {
        lastPos = message;
        List<Sentence> ss = new ArrayList<>();
        if (message.getPosition() != null) {
            GLLSentence gll = (GLLSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.GLL);
            gll.setStatus(DataStatus.ACTIVE);
            gll.setMode(FaaMode.AUTOMATIC);
            gll.setPosition(message.getPosition());
            Time t = new Time(message.getTimestamp().atZone(ZoneId.of("UTC")).format(fTIME));
            gll.setTime(t);
            ss.add(gll);
        }
        Collections.addAll(ss, handleRMC());
        return ss.toArray(TEMPLATE);
    }

    private Sentence[] handleHeading(N2KHeading message) {
        double heading = message.getHeading();
        String ref = message.getReference();
        if (!Double.isNaN(heading) && ref != null) {
            switch (ref) {
                case "Magnetic":
                    lastHeading = heading;
                    lastHeadingTime = System.currentTimeMillis();
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
        return TEMPLATE;
    }

    private Sentence[] handleSpeed(N2KSpeed message) {
        double speed = message.getSpeedWaterRef();
        if (!Double.isNaN(speed)) {
            VHWSentence vhw = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
            if (System.currentTimeMillis() - lastHeadingTime <= 1000) {
                vhw.setMagneticHeading(lastHeading);
            }
            vhw.setSpeedKnots(speed);
            vhw.setSpeedKmh(speed * 1.852);
            return new Sentence[]{vhw};
        }
        return TEMPLATE;
    }

    private Sentence[] handleWaterDepth(N2KWaterDepth message) {
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

    private Sentence[] handleWindData(N2KWindData message) {
        double windSpeed = message.getSpeed();
        double windAngle = message.getAngle();
        boolean apparent = message.isApparent();
        if (!Double.isNaN(windAngle) && !Double.isNaN(windSpeed)) {
            MWVSentence mwv = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
            mwv.setAngle(windAngle);
            mwv.setSpeed(windSpeed);
            mwv.setSpeedUnit(Units.KNOT);
            mwv.setTrue(!apparent);
            mwv.setStatus(DataStatus.ACTIVE);
            return new Sentence[]{mwv};
        }
        return TEMPLATE;
    }

    private Sentence[] handleAttitude(N2KAttitude message) {
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (!Double.isNaN(message.getYaw())) {
            double yaw = message.getYaw();
            xdr.addMeasurement(new Measurement("A", yaw, "D", "YAW"));
            send = true;
        }
        if (!Double.isNaN(message.getRoll())) {
            double roll = Utils.round(message.getYaw() - HWSettings.getPropertyAsDouble("gyro.roll", 0.0), 1);
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

    private Sentence[] handleEnvironment310(N2KEnvironment310 message) {
        double waterTemp = message.getWaterTemp();
        if (!Double.isNaN(waterTemp)) {
            MTWSentence s = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
            s.setTemperature(waterTemp);
            return new Sentence[]{s};
        }
        return TEMPLATE;
    }

    private Sentence[] handleEnvironment311(N2KEnvironment311 message) {
        double humidity = message.getHumidity();
        double airTemp = message.getTemperature();
        double pressure = message.getAtmosphericPressure();

        List<Sentence> res = new ArrayList<>();
        XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        boolean send = false;
        if (!Double.isNaN(airTemp)
                && "Main Cabin Temperature".equals(message.getTempSource())) {
            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTA);
            mta.setTemperature(airTemp);
            xdr.addMeasurement(new Measurement("C", Utils.round(airTemp, 1), "C", "CabinTemp"));
            res.add(mta);
            send = true;
        }
        if (!Double.isNaN(humidity)) {
            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MHU);
            mhu.setRelativeHumidity(humidity);
            xdr.addMeasurement(new Measurement("P", Utils.round(humidity, 2), "H", "Humidity"));
            res.add(mhu);
            send = true;
        }
        if (!Double.isNaN(pressure)) {
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MMB);
            mmb.setBars(pressure / 1000.0);
            mmb.setInchesOfMercury(Math.round(pressure * 760));
            xdr.addMeasurement(new Measurement("B", pressure, "B", "Barometer"));
            res.add(mmb);
            send = true;
        }
        if (send) {
            res.add(xdr);
        }
        return res.toArray(TEMPLATE);
    }

    private Sentence[] handleRudder(N2KRudder message) {
        int instance = message.getInstance();
        double angle = message.getPosition();
        if (instance == 0 && !Double.isNaN(angle)) {
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            rsa.setRudderAngle(Side.STARBOARD, Utils.normalizeDegrees180To180(angle));
            rsa.setStatus(Side.STARBOARD, DataStatus.ACTIVE);
            return new Sentence[]{rsa};
        } else {
            return TEMPLATE;
        }
    }

    private Sentence[] handleRateOfTurn(N2KRateOfTurn message) {
        double rate = message.getRate();
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
