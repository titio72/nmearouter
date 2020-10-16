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
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.message.*;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
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

    private String lastPolarFile;
    private PolarTable polars;
    private NMEASimulatorSourceSettings data;
    private final Random r = new Random();
    private final NavData navData = new NavData();
    private long lastTS = 0;

    private final Log log;

    @Inject
    public NMEASimulatorSourceX(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, true, true);
        this.log = log;
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
            if (data.getPolars() != null && !data.getPolars().equals(lastPolarFile)) {
                File f = new File(Constants.CONF_DIR, data.getPolars());
                try (FileReader reader = new FileReader(f)) {
                    polars.load(reader);
                }
                lastPolarFile = data.getPolars();
            }
        } catch (Exception e) {
            getLogBuilder().wO("load polars").error(log, e);
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
            getLogBuilder().wO("init").wV("error", "setup data missing").error(log);
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
            try {
                data.loadConf();
            } catch (IOException e) {
                getLogBuilder().wO("load setup data").error(log, e);
            }
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

            sendGPS(posOut, hdg, speed);
            sendDepth(depth);
            sendWind(absoluteWindSpeed, tWDirection, aWSpeed, aWDirection);
            sendHeadingAndSpeed(hdg, speed);
            sendMeteo(temp, press);
            sendGyro(hdg, roll, pitch);
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

    private void sendMeteo(double temp, double press) {
        if (data.isXdrMeteo()) {
            if (data.isXdrMeteoAtm()) notify(new MsgPressureImpl(PressureSource.ATMOSPHERIC, press));
            if (data.isXdrMeteoTmp()) notify(new MsgTemperatureImpl(TemperatureSource.MAIN_CABIN_ROOM, temp));
            if (data.isXdrMeteoHum()) notify(new MsgHumidityImpl(HumiditySource.INSIDE, data.getHum()));
            if (data.isMtw()) notify(new MsgTemperatureImpl(TemperatureSource.SEA, temp - 5));
        }
    }

    private void sendGyro(double hdg, double roll, double pitch) {
        if (data.isXdrGYR()) {
            notify(new MsgAttitudeImpl(hdg, roll, pitch));
        }
    }

    private void sendHeadingAndSpeed(double hdg, double speed) {
        if (data.isHdm()) {
            notify(new MsgHeadingImpl(hdg, true));
        }

        if (data.isVhw()) {
            notify(new MsgSpeedImpl(speed));
        }
    }

    private void sendWind(double absoluteWindSpeed, double tWDirection, double aWSpeed, double aWDirection) {
        if (data.isMwvA()) {
            notify(new MsgWindDataImpl(aWSpeed, aWDirection, true));
        }
        if (data.isMwvT()) {
            notify(new MsgWindDataImpl(absoluteWindSpeed, tWDirection, false));
        }
    }

    private void sendDepth(double depth) {
        if (data.isDpt()) {
            notify(new MsgWaterDepthImpl(depth, data.getDepthOffset()));
        }
    }

    private void sendGPS(Position posOut, double hdg, double speed) {
        if (data.isRmc()) {
            MsgGNSSPositionImpl p = new MsgGNSSPositionImpl(posOut, Instant.now(), 10, 0.72, Double.NaN);
            MsgSOGAndCOGImpl s = new MsgSOGAndCOGImpl(speed, hdg);
            MsgPositionAndVector ps = new MsgPositionAndVectorFacade(p, s);
            notify(s);
            notify(p);
            notify(ps);

            notify(new MsgSystemTimeImpl("GPS", p.getTimestamp()));
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
