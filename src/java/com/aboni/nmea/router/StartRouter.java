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

package com.aboni.nmea.router;

import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.conf.LogLevelType;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.sensors.HMC5883Calibration;
import com.aboni.sensors.SensorHMC5883;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.LogAdmin;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Properties;

public class StartRouter {

    private static final String CALIBRATION = "-cal";
    private static final String PLAY = "-play";
    private static final String HELP = "-help";

    private static int checkFlag(String flag, String[] args) {
        if (args!=null) {
            for (int i = 0; i<args.length; i++) {
                if (flag.equals(args[i])) return i;
            }
        }
        return -1;
    }

    private static void consoleOut(String s) {
        ConsoleLog.getLogger().info(s);
    }

    private static LogAdmin logAdmin = null;

    public static void main(@NotNull String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        logAdmin = ThingsFactory.getInstance(LogAdmin.class);
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
                    logAdmin.error(String.format("Uncaught error {%s} thread {%s}", e.getMessage(), t.getName()), e);
                }
        );
        ConfJSON cJ;
        try {
            cJ = new ConfJSON();
            LogLevelType logLevel = cJ.getLogLevel();
            configureLog(logLevel);
        } catch (Exception e) {
            consoleOut("Cannot read configuration:" + e.getMessage());
        }

        int ix;
        if (checkFlag(HELP, args) >= 0) {
            consoleOut("-sensor : sensor monitor\r\n" +
                    "-play : NMEA file to play\r\n" +
                    "-cal : compass calibration\r\n");
        } else if ((ix = checkFlag(PLAY, args)) >= 0) {
            Properties props = new Properties();
            props.setProperty("file", args[ix + 1]);
            startRouter(injector, ThingsFactory.getInstance(NMEARouterBuilder.class, "play"), props);
        } else if (checkFlag(CALIBRATION, args) >= 0) {
            startCalibration();
        } else {
            Properties props = new Properties();
            startRouter(injector, ThingsFactory.getInstance(NMEARouterBuilder.class, "router"), props);
        }
    }

    private static void startCalibration() {
        SensorHMC5883 m = new SensorHMC5883(logAdmin);
        m.setDefaultSmoothingAlpha(1.0);
        try {
            m.init(1);
            HMC5883Calibration cc = new HMC5883Calibration(m, 15L * 1000L);
            consoleOut("Start");
            cc.start();
            consoleOut("Radius: " + cc.getRadius());
            consoleOut("StdDev: " + cc.getsDev());
            consoleOut("StdDev: " + cc.getsDev());
            consoleOut("C_X:    " + cc.getCalibration()[0]);
            consoleOut("C_Y:    " + cc.getCalibration()[1]);
            consoleOut("C_Z:    " + cc.getCalibration()[2]);
        } catch (Exception e1) {
            logAdmin.error("Error during calibration", e1);
        }
    }

    private static void startRouter(Injector injector, NMEARouterBuilder builder, Properties p) {
        logAdmin.infoFill("");
        logAdmin.infoFill("NMEARouter");
        logAdmin.infoFill("");
        logAdmin.infoFill("Start " + ZonedDateTime.now());
        logAdmin.infoFill("");
        NMEAUtils.registerExtraSentences();
        injector.getInstance(NMEAStream.class); // be sure the stream started
        NMEARouter router = ThingsFactory.getInstance(NMEARouter.class);
        builder.init(router, p);
        router.start();
    }


    private static void configureLog(LogLevelType level) {
        LogAdmin logger = ThingsFactory.getInstance(LogAdmin.class);
        switch (level) {
            case DEBUG:
                logger.setDebug();
                break;
            case WARNING:
                logger.setWarning();
                break;
            case ERROR:
                logger.setError();
                break;
            case NONE:
                logger.setNone();
                break;
            default:
                logger.setInfo();
                break;
        }
    }
}
