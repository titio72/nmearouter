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
import com.aboni.misc.PolarTable;
import com.aboni.misc.PolarTableImpl;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.seatalk.Stalk84;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

@SuppressWarnings("OverlyComplexClass")
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
        int headingAuto = Integer.MIN_VALUE;
        double refHeading = Double.NaN;
    }

    private String lastPolarFile;
    private PolarTable polars;
    private NMEASimulatorSourceSettings data;
    private final TalkerId id;
    private final Random r = new Random();
    private final NavData navData = new NavData();
    private long lastTS = 0;

    @Inject
    public NMEASimulatorSource(@NotNull NMEACache cache) {
        super(cache);
        setSourceTarget(true, true);
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
        try {
            if (data.getPolars()!=null && !data.getPolars().equals(lastPolarFile)) {
                File f = new File(Constants.CONF_DIR, data.getPolars());
                try (FileReader reader = new FileReader(f)) {
                    polars.load(reader);
                }
                lastPolarFile = data.getPolars();
            }
        } catch (Exception e) {
            getLogger().error("Cannot load polars", e);
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
            getLogger().error("Cannot start Simulator - setup missing!");
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
        if (isStarted()) {
            data.loadConf();
            loadPolars();

            Position posOut = new Position(navData.pos.getLatitude(), navData.pos.getLongitude());

            navData.refHeading = data.getHeading();

            long newTS = System.currentTimeMillis();
            double ph15m = System.currentTimeMillis() / (1000d * 60d * 15d) * 2 * Math.PI; // 15 minutes phase
            double depth = Utils.round(data.getDepth() + Math.sin(ph15m) * data.getDepthRange(), 1);
            double hdg = Utils.normalizeDegrees0To360(navData.refHeading + r.nextDouble() * 3.0);
            double absoluteWindSpeed = data.getWindSpeed() + r.nextDouble() * 1.0;
            double absoluteWindDir = data.getWindDirection() + r.nextDouble() * 2.0;
            double tWDirection = Utils.normalizeDegrees0To360(absoluteWindDir - hdg);
            double speed = getSpeed((float) absoluteWindSpeed, (int) tWDirection);

            navData.distance += speed / 3600.0;
            navData.trip += speed / 3600.0;

            ApparentWind aWind = new ApparentWind(speed, tWDirection, absoluteWindSpeed);
            double aWSpeed = aWind.getApparentWindSpeed();
            double aWDirection = Utils.normalizeDegrees0To360(aWind.getApparentWindDeg());

            double temp = Utils.round(data.getTemp() + (new Random().nextDouble() / 10.0), 2);
            double press = Utils.round(data.getPress() + (new Random().nextDouble() / 10.0), 1);
            double roll = Utils.round(new Random().nextDouble() * 5, 1);
            double pitch = Utils.round((new Random().nextDouble() * 5) + 0, 1);

            if (lastTS != 0) {
                double dTime = (double) (newTS - lastTS) / 1000d / 60d / 60d;
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
            sendAP();
            sendRSA();
        }
    }

    private void addGpsNoise(Position posOut) {
        if (data.isGpsOut()) {
            int x = r.nextInt(25);
            if (x == 0) {
                if (r.nextBoolean())
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
            speed = Utils.round(data.getSpeed() * (1.0 + r.nextDouble() / 10.0), 1);
        }
        return speed;
    }

    private void sendAP() {
        if (data.isAutoPilot()) {
            sendAutopilotStatus();
        }
    }

    private void sendRSA() {
        if (data.isRsa()){
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            rsa.setRudderAngle(Side.STARBOARD, data.getRudder());
            NMEASimulatorSource.this.notify(rsa);
        }
    }

    private void sendVoltage() {
        if (data.isXdrDiagnostic()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            xdr.addMeasurement(new Measurement("V", 13.56, "V", "V0"));
            xdr.addMeasurement(new Measurement("V", 13.12, "V", "V1"));
            NMEASimulatorSource.this.notify(xdr);
        }
    }

    private void sendMeteo(double hdg, double absoluteWindSpeed, double tWDirection, double temp, double press) {
        if (data.isXdrMeteo()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());

            if (data.isXdrMeteoAtm()) xdr.addMeasurement(new Measurement("P", press / 1000, "B", "Barometer"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp + 1, "C", "CabinTemp"));
            if (data.isXdrMeteoHum()) xdr.addMeasurement(new Measurement("C", data.getHum(), "H", "Humidity"));
            NMEASimulatorSource.this.notify(xdr);
        }

        if (data.isMtw()) {
            MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
            t.setTemperature(temp - 5);
            NMEASimulatorSource.this.notify(t);
        }

        if (data.isMta()) {
            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
            mta.setTemperature(temp);
            NMEASimulatorSource.this.notify(mta);
        }

        if (data.isMbb()) {
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
            mmb.setBars(press / 1000.0);
            NMEASimulatorSource.this.notify(mmb);
        }

        if (data.isMhu()) {
            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
            mhu.setRelativeHumidity(data.getHum());
            NMEASimulatorSource.this.notify(mhu);
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
            NMEASimulatorSource.this.notify(mda);
        }
    }

    private void sendGyro(double hdg, double roll, double pitch) {
        if (data.isXdrGYR()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
            xdr.addMeasurement(new Measurement("A", hdg, "D", "HEAD"));
            xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
            xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
            NMEASimulatorSource.this.notify(xdr);
        }
    }

    private void sendHeadingAndSpeed(double hdg, double speed) {
        if (data.isHdm()) {
            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
            hdm.setHeading(hdg);
            NMEASimulatorSource.this.notify(hdm);
        }

        if (data.isHdt()) {
            HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
            hdt.setHeading(hdg);
            NMEASimulatorSource.this.notify(hdt);
        }

        if (data.isHdg()) {
            HDGSentence hdgS = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
            hdgS.setHeading(hdg);
            NMEASimulatorSource.this.notify(hdgS);
        }

        if (data.isVhw()) {
            VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
            s.setHeading(hdg);
            s.setMagneticHeading(hdg);
            s.setSpeedKnots(speed);
            s.setSpeedKmh(speed * 1.852);
            NMEASimulatorSource.this.notify(s);
        }
    }

    private void sendSplitApparentWind(double aWSpeed, double aWDirection) {
        MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        v.setAngle(aWDirection);
        v.setTrue(false);
        v.setSpeedUnit(Units.KNOT);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.notify(v);

        v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        v.setTrue(false);
        v.setSpeedUnit(Units.KMH);
        v.setSpeed(aWSpeed * 1.852);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.notify(v);
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
            NMEASimulatorSource.this.notify(vwr);
        }

        if (data.isVwr()) {
            VWTSentence vwt = (VWTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWT");
            vwt.setWindAngle(tWDirection > 180 ? 360 - tWDirection : tWDirection);
            vwt.setSpeedKnots(absoluteWindSpeed);
            vwt.setDirectionLeftRight(tWDirection > 180 ? Direction.LEFT : Direction.RIGHT);
            NMEASimulatorSource.this.notify(vwt);
        }
    }

    private void sendApparentWind(double aWSpeed, double aWDirection, boolean b) {
        MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        setWindSpeedValues(aWSpeed, v);
        v.setAngle(aWDirection);
        v.setTrue(b);
        v.setStatus(DataStatus.ACTIVE);
        NMEASimulatorSource.this.notify(v);
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
            NMEASimulatorSource.this.notify(d);
        }

        if (data.isDbt()) {
            DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DBT);
            d.setDepth(depth);
            NMEASimulatorSource.this.notify(d);
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
            NMEASimulatorSource.this.notify(rmc);
        }

        if (data.isGll()) {
            GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
            s1.setPosition(posOut);
            s1.setStatus(DataStatus.ACTIVE);
            s1.setTime(new Time());
            NMEASimulatorSource.this.notify(s1);
        }

        if (data.isVtg()) {
            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VTG);
            vtg.setMagneticCourse(hdg);
            vtg.setTrueCourse(hdg);
            vtg.setMode(FaaMode.AUTOMATIC);
            vtg.setSpeedKnots(speed);
            vtg.setSpeedKmh(speed * 1.852);
            NMEASimulatorSource.this.notify(vtg);
        }
    }

    private void sendVLW() {
        if (data.isVlw()) {
            VLWSentence s = (VLWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VLW);
            s.setTotal(navData.distance);
            s.setTotalUnits('N');
            s.setTrip(navData.trip);
            s.setTripUnits('N');
            NMEASimulatorSource.this.notify(s);
        }
    }

    private void sendAutopilotStatus() {
        Stalk84 s84 = new Stalk84(
                (int) navData.refHeading, (navData.headingAuto == Integer.MIN_VALUE) ? 0 : navData.headingAuto, 0,
                (navData.headingAuto == Integer.MIN_VALUE) ? Stalk84.STATUS.STATUS_STANDBY : Stalk84.STATUS.STATUS_AUTO,
                Stalk84.ERROR.ERROR_NONE, Stalk84.TURN.STARBOARD);
        STALKSentence stalk = (STALKSentence) SentenceFactory.getInstance().createParser(s84.getSTALKSentence());
        NMEASimulatorSource.this.notify(stalk);
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (!source.equals(getName()) && s instanceof STALKSentence) {
            STALKSentence t = (STALKSentence) s;
            if ("86".equals(t.getCommand())) {
                String[] p = t.getParameters();
                if ("21".equals(p[0])) {
                    handleAPStatusCommands(p);
                } else if ("11".equals(p[0])) {
                    handleAPDirectionCommands(p);
                }
            }

        }

    }

    private void handleAPStatusCommands(String[] p) {
        String disc = p[1] + p[2];
        switch (disc) {
            case "01FE":
                if (navData.headingAuto == Integer.MIN_VALUE) {
                    navData.headingAuto = (int) navData.refHeading;
                    sendAutopilotStatus();
                }
                break;
            case "02FD":
                if (navData.headingAuto != Integer.MIN_VALUE) {
                    navData.headingAuto = Integer.MIN_VALUE;
                    sendAutopilotStatus();
                }
                break;
            default:
                break;
        }
    }

    private void  handleAPDirectionCommands(String[] p) {
        String disc = p[1] + p[2];
        int delta;
        switch (disc) {
            case "05FA": delta = -1; break;
            case "06F9": delta = -10; break;
            case "07F8": delta = 1; break;
            case "08F7": delta = 10; break;
            default: delta = 0; break;
        }
        if (delta != 0) {
            if (navData.headingAuto != Integer.MIN_VALUE) {
                navData.headingAuto += delta;
                sendAutopilotStatus();
            }
            navData.refHeading += delta;
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
