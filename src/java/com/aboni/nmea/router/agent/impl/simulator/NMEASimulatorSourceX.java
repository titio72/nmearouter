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
import com.aboni.nmea.message.*;
import com.aboni.nmea.message.impl.*;
import com.aboni.nmea.router.Constants;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.utils.PolarTable;
import com.aboni.utils.PolarTableImpl;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.Random;

@SuppressWarnings("OverlyCoupledClass")
public class NMEASimulatorSourceX extends NMEAAgentImpl implements SimulatorDriver {

    private static NMEASimulatorSourceX simulator;

    public static NMEASimulatorSourceX getSimulator() {
        return simulator;
    }

    public static void setSimulator(NMEASimulatorSourceX s) {
        simulator = s;
    }

    private static class NavData {
        Position pos = new Position(43.9599, 09.7745);
        double refHeading = Double.NaN;
    }

    private static class PowerData {
        boolean charging = false;
        double voltage = 13.2; // volt
        double current = 3.45; // ampere
        double temperature = 28.2; //celsius
        double totAh = -280 * (1 - 0.365);

        double getSOC() {
            return (280.0 + totAh) / 280.0;
        }
    }

    private String lastPolarFile;
    private PolarTable polars;
    private NMEASimulatorSourceSettings data;
    private final Random r = new Random();
    private final NavData navData = new NavData();
    private final PowerData powerData = new PowerData();
    private long lastTS = 0;

    @Inject
    public NMEASimulatorSourceX(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, true);
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
        if (data.getPolars() != null && !data.getPolars().equals(lastPolarFile)) {
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

    private static double oscillator(double base, int periodMinutes, double amplitude) {
        return base + amplitude * Math.sin(Math.PI * 0.5 * (System.currentTimeMillis() / (double) periodMinutes / 60.0 / 1000.0));
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
            double hdg = Utils.normalizeDegrees0To360(navData.refHeading + r.nextDouble() * 3.0);
            double absoluteWindSpeed = data.getWindSpeed() + r.nextDouble();
            double absoluteWindDir = data.getWindDirection() + r.nextDouble() * 2.0;
            double tWDirection = Utils.normalizeDegrees0To360(absoluteWindDir - hdg);
            double speed = getSpeed((float) absoluteWindSpeed, (int) tWDirection);

            ApparentWind aWind = new ApparentWind(speed, tWDirection, absoluteWindSpeed);
            double aWSpeed = aWind.getApparentWindSpeed();
            double aWDirection = Utils.normalizeDegrees0To360(aWind.getApparentWindDeg());

            double temp = Utils.round(oscillator(data.getTemp(), 60, 5.0), 2);
            double press = Utils.round(oscillator(data.getPress(), 180, 4), 1);
            double humidity = Utils.round(oscillator(data.getHum(), 60, 2.0), 2);
            double roll = Utils.round(r.nextDouble() * 5, 1) * absoluteWindDir < 180 ? -1 : 1;
            double pitch = Utils.round((r.nextDouble() * 5) + 0, 1);
            double yaw = Utils.normalizeDegrees180To180(hdg);

            if (lastTS != 0) {
                double dTime = (newTS - lastTS) / 1000d / 60d / 60d;
                navData.pos = Utils.calcNewLL(navData.pos, hdg, speed * dTime);
                posOut = new Position(navData.pos.getLatitude(), navData.pos.getLongitude());
                addGpsNoise(posOut);
            }
            lastTS = newTS;

            if (powerData.charging)
                powerData.current = 14.45 + r.nextDouble();
            else
                powerData.current = -21.45 - r.nextDouble();

            powerData.totAh += powerData.current / 3600;
            powerData.voltage = 14.0 + 2.0 * (powerData.totAh / 280.0);
            powerData.temperature = 28.2;

            if (powerData.totAh < -250) powerData.charging = true;
            else if (powerData.totAh > -10) powerData.charging = false;

            sendPower(powerData);

            sendGPS(posOut, hdg, speed);
            sendDepth(depth);
            sendWind(absoluteWindSpeed, tWDirection, aWSpeed, aWDirection);
            sendHeadingAndSpeed(hdg, speed);
            sendMeteo(temp, press, humidity);
            sendGyro(yaw, roll, pitch);
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

    private void sendMeteo(double temp, double press, double hum) {
        if (data.isXdrMeteo()) {
            if (data.isXdrMeteoAtm()) postMessage(new MsgPressureImpl(PressureSource.ATMOSPHERIC, press));
            if (data.isXdrMeteoTmp()) postMessage(new MsgTemperatureImpl(TemperatureSource.MAIN_CABIN_ROOM, temp));
            if (data.isXdrMeteoHum()) postMessage(new MsgHumidityImpl(HumiditySource.INSIDE, hum));
            if (data.isMtw()) postMessage(new MsgTemperatureImpl(TemperatureSource.SEA, temp - 5));
        }
    }

    private void sendPower(PowerData powerData) {
        postMessage(new MsgBatteryImpl(0, 1, 12.7, Double.NaN, Double.NaN));
        postMessage(new MsgBatteryImpl(0, 0, powerData.voltage, powerData.current, powerData.temperature));
        postMessage(new MsgDCDetailedStatusImpl(0, 0, DCType.BATTERY, powerData.getSOC(), 1.0,
                -(int) ((280 + powerData.totAh) / powerData.current), Double.NaN));
    }

    private void sendGyro(double hdg, double roll, double pitch) {
        if (data.isXdrGYR()) {
            postMessage(new MsgAttitudeImpl(hdg, roll, pitch));
        }
    }

    private void sendHeadingAndSpeed(double hdg, double speed) {
        if (data.isHdm()) {
            postMessage(new MsgHeadingImpl(hdg, true));
        }

        if (data.isVhw()) {
            postMessage(new MsgSpeedImpl(speed));
            postMessage(new MsgSpeedAndHeadingFacade(new MsgSpeedImpl(speed), new MsgHeadingImpl(hdg, true)));
        }
    }

    private void sendWind(double absoluteWindSpeed, double tWDirection, double aWSpeed, double aWDirection) {
        if (data.isMwvA()) {
            postMessage(new MsgWindDataImpl(aWSpeed, aWDirection, true));
        }
        if (data.isMwvT()) {
            postMessage(new MsgWindDataImpl(absoluteWindSpeed, tWDirection, false));
        }
    }

    private void sendDepth(double depth) {
        if (data.isDpt()) {
            postMessage(new MsgWaterDepthImpl(depth, data.getDepthOffset()));
        }
    }

    private void sendGPS(Position posOut, double hdg, double speed) {
        if (data.isRmc()) {
            MsgGNSSPositionImpl p = new MsgGNSSPositionImpl(posOut, Instant.now(), 10, 0.72, Double.NaN);
            MsgSOGAndCOGImpl s = new MsgSOGAndCOGImpl(speed, hdg);
            MsgPositionAndVector ps = new MsgPositionAndVectorFacade(p, s);
            postMessage(s);
            postMessage(p);
            postMessage(ps);
            postMessage(new MsgSystemTimeImpl("GPS", p.getTimestamp()));
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
