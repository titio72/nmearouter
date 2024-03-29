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

package com.aboni.nmea.router.agent.impl.simulator;

import com.aboni.geo.ApparentWind;
import com.aboni.log.Log;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.utils.PolarTable;
import com.aboni.utils.PolarTableImpl;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class NMEASimulatorSource extends NMEAAgentImpl implements SimulatorDriver {

    private static NMEASimulatorSource simulator;

    public static NMEASimulatorSource getSimulator() {
        return simulator;
    }

    public static void setSimulator(NMEASimulatorSource s) {
        simulator = s;
    }

    private static class NavData {
        Position pos = new Position(43.9599, 09.7745);
        double distance = 0;
        double trip = 0;
        double refHeading = Double.NaN;
    }

    private String lastPolarFile;
    private PolarTable polars;
    private NMEASimulatorSourceSettings data;
    private final TalkerId id;
    private final NavData navData = new NavData();
    private long lastTS = 0;

    private final Random rnd = new Random();

    @Inject
    public NMEASimulatorSource(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, true);
        id = TalkerId.GP;
        polars = null;
    }

    @Override
    public String getType() {
        return "Simulator";
    }

    private void loadPolars() {
        if (polars == null) {
            polars = new PolarTableImpl();
        }
        if (data.getPolars()!=null && !data.getPolars().equals(lastPolarFile)) {
            File f = new File(Constants.CONF_DIR, data.getPolars());
            try (FileReader reader = new FileReader(f)) {
                polars.load(reader);
            } catch (Exception e) {
                getLog().error(() -> getLogBuilder().wO("load polars").toString(), e);
            }
            lastPolarFile = data.getPolars();
        }
    }

    @Override
    protected void onSetup(String name, QOS qos) {
        data = new NMEASimulatorSourceSettings("sim_" + name + ".properties");
        if (simulator == null) setSimulator(this);
    }

    @Override
    protected boolean onActivate() {
        if (data != null) {
            return true;
        } else {
            getLog().error(() -> getLogBuilder().wO("init").wV("error", "setup data missing").toString());
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Simulator";
    }

    @Override
    public double getHeading() {
        return navData.refHeading;
    }

    @Override
    public void setHeading(double heading) {
        navData.refHeading = heading;
    }

    @Override
    public double getWindSpeed() {
        return data.getWindSpeed();
    }

    @Override
    public void setWindSpeed(double wSpeed) {
        this.data.setWindSpeed(wSpeed);
    }

    @Override
    public double getWindDirection() {
        return data.getWindDirection();
    }

    @Override
    public void setWindDirection(double wDirection) {
        this.data.setWindDirection(wDirection);
    }

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            try {
                data.loadConf();
            } catch (IOException e) {
                getLog().error(() -> getLogBuilder().wO("load setup data").toString(), e);
            }
            loadPolars();

            Position posOut = new Position(navData.pos.getLatitude(), navData.pos.getLongitude());

            navData.refHeading = data.getHeading();

            long newTS = System.currentTimeMillis();
            double ph15m = System.currentTimeMillis() / (1000d * 60d * 15d) * 2 * Math.PI; // 15 minutes phase
            double depth = Utils.round(data.getDepth() + Math.sin(ph15m) * data.getDepthRange(), 1);
            double hdg = Utils.normalizeDegrees0To360(navData.refHeading + rnd.nextDouble() * 3.0);
            double absoluteWindSpeed = data.getWindSpeed() + rnd.nextDouble();
            double absoluteWindDir = data.getWindDirection() + rnd.nextDouble() * 2.0;
            double tWDirection = Utils.normalizeDegrees0To360(absoluteWindDir - hdg);
            double speed = getSpeed((float) absoluteWindSpeed, (int) tWDirection);

            navData.distance += speed / 3600.0;
            navData.trip += speed / 3600.0;

            ApparentWind aWind = new ApparentWind(speed, tWDirection, absoluteWindSpeed);
            double aWSpeed = aWind.getApparentWindSpeed();
            double aWDirection = Utils.normalizeDegrees0To360(aWind.getApparentWindDeg());

            double temp = Utils.round(data.getTemp() + (rnd.nextDouble() / 10.0), 2);
            double press = Utils.round(data.getPress() + Math.sin(Math.PI * (System.currentTimeMillis() / 3600000.0) / 2.0), 1);
            double roll = Utils.round(rnd.nextDouble() * 5, 1);
            double pitch = Utils.round((rnd.nextDouble() * 5) + 0, 1);

            if (lastTS != 0) {
                double dTime = (newTS - lastTS) / 1000d / 60d / 60d;
                navData.pos = Utils.calcNewLL(navData.pos, hdg, speed * dTime);
                posOut = new Position(navData.pos.getLatitude(), navData.pos.getLongitude());
                addGpsNoise(posOut);
            }
            lastTS = newTS;

            sendVLW();
            sendGPS(posOut, hdg, speed);
            sendDepth(depth);
            sendWind(absoluteWindSpeed, tWDirection, aWSpeed, aWDirection);
            sendHeadingAndSpeed(hdg, speed);
            sendMeteo(hdg, absoluteWindSpeed, tWDirection, temp, press);
            sendGyro(hdg, roll, pitch);
            sendVoltage();
            sendRSA();
        }
    }

    private void addGpsNoise(Position posOut) {
        if (data.isGpsOut()) {
            int x = rnd.nextInt(25);
            if (x == 0) {
                if (rnd.nextBoolean())
                    posOut.setLongitude(navData.pos.getLongitude() + 1.0);
                else
                    posOut.setLatitude(navData.pos.getLatitude() + 1.0);
            }
        }
    }

    private double getSpeed(float absoluteWindSpeed, int tWDirection) {
        double speed;
        if (data.isUsePolars()) {
            speed = polars.getSpeed(tWDirection, absoluteWindSpeed) * data.getPolarCoefficient();
        } else {
            speed = Utils.round(data.getSpeed() * (1.0 + rnd.nextDouble() / 10.0), 1);
        }
        return speed;
    }

    private void sendRSA() {
        if (data.isRsa()){
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            rsa.setRudderAngle(Side.STARBOARD, data.getRudder());
            NMEASimulatorSource.this.postMessage(rsa);
        }
    }

    private void sendVoltage() {
        if (data.isXdrDiagnostic()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            xdr.addMeasurement(new Measurement("V", 13.56, "V", "VOLTAGE0"));
            xdr.addMeasurement(new Measurement("V", 13.12, "V", "VOLTAGE1"));
            NMEASimulatorSource.this.postMessage(xdr);
        }
    }

    private void sendMeteo(double hdg, double absoluteWindSpeed, double tWDirection, double temp, double press) {
        if (data.isXdrMeteo()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());

            if (data.isXdrMeteoAtm()) xdr.addMeasurement(new Measurement("P", press / 1000, "B", "Barometer"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp + 1, "C", "CabinTemp"));
            if (data.isXdrMeteoHum()) xdr.addMeasurement(new Measurement("C", data.getHum(), "H", "Humidity"));
            NMEASimulatorSource.this.postMessage(xdr);
        }

        if (data.isMtw()) {
            MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
            t.setTemperature(temp - 5);
            NMEASimulatorSource.this.postMessage(t);
        }

        if (data.isMta()) {
            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
            mta.setTemperature(temp);
            NMEASimulatorSource.this.postMessage(mta);
        }

        if (data.isMbb()) {
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
            mmb.setBars(press / 1000.0);
            NMEASimulatorSource.this.postMessage(mmb);
        }

        if (data.isMhu()) {
            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
            mhu.setRelativeHumidity(data.getHum());
            NMEASimulatorSource.this.postMessage(mhu);
        }

        if (data.isMda()) {
            MDASentence mda = (MDASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MDA");
            mda.setRelativeHumidity(data.getHum());
            mda.setAirTemperature(temp);
            mda.setPrimaryBarometricPressure(press * 750.06375541921);
            mda.setPrimaryBarometricPressureUnit('I');
            mda.setSecondaryBarometricPressure(press / 1000.0);
            mda.setSecondaryBarometricPressureUnit('B');
            mda.setWaterTemperature(temp - 5);
            mda.setMagneticWindDirection(tWDirection + hdg);
            mda.setTrueWindDirection(tWDirection + hdg);
            mda.setWindSpeedKnots(absoluteWindSpeed);
            NMEASimulatorSource.this.postMessage(mda);
        }
    }

    private void sendGyro(double hdg, double roll, double pitch) {
        if (data.isXdrGYR()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            xdr.addMeasurement(new Measurement("A", hdg, "D", "HEAD"));
            xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
            xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
            NMEASimulatorSource.this.postMessage(xdr);
        }
    }

    private void sendHeadingAndSpeed(double hdg, double speed) {
        if (data.isHdm()) {
            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
            hdm.setHeading(hdg);
            NMEASimulatorSource.this.postMessage(hdm);
        }

        if (data.isHdt()) {
            HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
            hdt.setHeading(hdg);
            NMEASimulatorSource.this.postMessage(hdt);
        }

        if (data.isHdg()) {
            HDGSentence hdgS = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
            hdgS.setHeading(hdg);
            NMEASimulatorSource.this.postMessage(hdgS);
        }

        if (data.isVhw()) {
            VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
            s.setHeading(hdg);
            s.setMagneticHeading(hdg);
            s.setSpeedKnots(speed);
            s.setSpeedKmh(speed * 1.852);
            NMEASimulatorSource.this.postMessage(s);
        }
    }

    private void sendSplitApparentWind(double aWSpeed, double aWDirection) {
        MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        v.setAngle(aWDirection);
        v.setTrue(false);
        v.setSpeedUnit(Units.KNOT);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.postMessage(v);

        v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        v.setTrue(false);
        v.setSpeedUnit(Units.KMH);
        v.setSpeed(aWSpeed * 1.852);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.postMessage(v);
    }

    private void sendWind(double absoluteWindSpeed, double tWDirection, double aWSpeed, double aWDirection) {
        if (data.isMwvA()) {
            if (data.isSplitWind()) {
                sendSplitApparentWind(aWSpeed, aWDirection);
            } else {
                sendApparentWind(aWSpeed, aWDirection, false);
            }
        }

        if (data.isMwvT()) {
            sendApparentWind(absoluteWindSpeed, tWDirection, true);
        }

        if (data.isVwr()) {
            VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWR");
            vwr.setAngle(aWDirection>180?360-aWDirection:aWDirection);
            vwr.setSpeed(aWSpeed);
            vwr.setSide(Side.PORT);
            vwr.setStatus(DataStatus.ACTIVE);
            NMEASimulatorSource.this.postMessage(vwr);
        }

        if (data.isVwr()) {
            VWTSentence vwt = (VWTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWT");
            vwt.setWindAngle(tWDirection > 180 ? 360 - tWDirection : tWDirection);
            vwt.setSpeedKnots(absoluteWindSpeed);
            vwt.setDirectionLeftRight(tWDirection > 180 ? Direction.LEFT : Direction.RIGHT);
            NMEASimulatorSource.this.postMessage(vwt);
        }
    }

    private void sendApparentWind(double aWSpeed, double aWDirection, boolean b) {
        MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        setWindSpeedValues(aWSpeed, v);
        v.setAngle(aWDirection);
        v.setTrue(b);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.postMessage(v);
    }

    private void setWindSpeedValues(double aWSpeed, MWVSentence v) {
        if (data.isWindSpeedInMS()) {
            v.setSpeedUnit(Units.METER);
            v.setSpeed(aWSpeed / 1.947);
        } else {
            v.setSpeedUnit(Units.KNOT);
            v.setSpeed(aWSpeed);
        }
    }

    private void sendDepth(double depth) {
        if (data.isDpt()) {
            DPTSentence d = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
            d.setDepth(depth);
            d.setOffset(data.getDepthOffset());
            NMEASimulatorSource.this.postMessage(d);
        }

        if (data.isDbt()) {
            DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DBT);
            d.setDepth(depth);
            NMEASimulatorSource.this.postMessage(d);
        }
    }

    private void sendGPS(Position posOut, double hdg, double speed) {
        if (data.isRmc()) {
            RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
            rmc.setCourse(hdg);

            rmc.setStatus(DataStatus.ACTIVE);

            Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Date ddd = new Date();
            ddd.setDay(c.get(Calendar.DAY_OF_MONTH));
            ddd.setMonth(c.get(Calendar.MONTH) + 1);
            ddd.setYear(c.get(Calendar.YEAR));
            rmc.setDate(ddd);
            Time ttt = new Time();
            ttt.setHour(c.get(Calendar.HOUR_OF_DAY));
            ttt.setMinutes(c.get(Calendar.MINUTE));
            ttt.setSeconds(c.get(Calendar.SECOND));
            rmc.setTime(ttt);

            rmc.setVariation(0.0);
            rmc.setMode(FaaMode.AUTOMATIC);
            rmc.setDirectionOfVariation(CompassPoint.WEST);
            rmc.setSpeed(speed);
            rmc.setPosition(posOut);
            NMEASimulatorSource.this.postMessage(rmc);
        }

        if (data.isGll()) {
            GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
            s1.setPosition(posOut);
            s1.setStatus(DataStatus.ACTIVE);
            s1.setTime(new Time());
            NMEASimulatorSource.this.postMessage(s1);
        }

        if (data.isVtg()) {
            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VTG);
            vtg.setMagneticCourse(hdg);
            vtg.setTrueCourse(hdg);
            vtg.setMode(FaaMode.AUTOMATIC);
            vtg.setSpeedKnots(speed);
            vtg.setSpeedKmh(speed * 1.852);
            NMEASimulatorSource.this.postMessage(vtg);
        }
    }

    private void sendVLW() {
        if (data.isVlw()) {
            VLWSentence s = (VLWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VLW);
            s.setTotal(navData.distance);
            s.setTotalUnits('N');
            s.setTrip(navData.trip);
            s.setTripUnits('N');
            NMEASimulatorSource.this.postMessage(s);
        }
    }

    @Override
    public double getSpeed() {
        return data.getSpeed();
    }

    @Override
    public void setSpeed(double speed) {
        this.data.setSpeed(speed);
    }
}
