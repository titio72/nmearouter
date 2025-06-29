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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSourceX;
import com.aboni.nmea.router.agent.BuiltInAgents;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.agent.impl.system.*;
import com.aboni.nmea.router.conf.*;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;

@SuppressWarnings({"OverlyCoupledClass", "OverlyComplexClass", "ClassWithTooManyMethods"})
public class NMEAAgentBuilderJsonImpl implements NMEAAgentBuilderJson {

    public static final String DEVICE_ATTRIBUTE = "device";
    public static final String PORT_ATTRIBUTE = "port";
    public static final String HOST_ATTRIBUTE = "host";
    public static final String AGENT_BUILDER_CATEGORY = "AgentBuilder";
    public static final String BUILD_AGENT_KEY_NAME = "build agent";

    private final Log log;

    @Inject
    public NMEAAgentBuilderJsonImpl(Log log) {
        this.log = SafeLog.getSafeLog(log);
    }

    @Override
    public NMEAAgent createAgent(BuiltInAgents agent) {
        switch (agent) {
            case WEB_UI:
                return buildWebUI();
            case POWER_LED:
                return buildPowerLedTarget();
            case AUTO_PILOT:
                return buildAutoPilot();
            case EVO_AUTO_PILOT:
                return buildEvoAutoPilot();
            case DEPTH_STATS:
                return buildDPTStats();
            case FAN_MANAGER:
                return buildFanTarget();
            case FILE_DUMPER:
                return buildStreamDump();
            case ENGINE_DETECTOR:
                return buildEngineDetector();
            case GPS_TIME_SYNC:
                return buildGPSTimeTarget();
            default:
                return null;
        }
    }

    @Override
    public NMEAAgent createAgent(AgentConfJSON a) {
        NMEAAgent agent = null;
        QOS q = a.getQos();
        switch (a.getType()) {
            case AgentTypes.NEXTION:
                agent = buildNextion(a, q);
                break;
            case AgentTypes.SIMULATOR_X:
                agent = buildStandard(a, q, NMEASimulatorSourceX.class, AgentTypes.SIMULATOR);
                break;
            case AgentTypes.SIMULATOR:
                agent = buildStandard(a, q, NMEASimulatorSource.class, AgentTypes.SIMULATOR);
                break;
            case AgentTypes.SENSOR:
                agent = buildStandard(a, q, NMEASourceSensor.class, AgentTypes.SENSOR);
                break;
            case AgentTypes.GYRO:
                agent = buildStandard(a, q, NMEASourceGyro.class, AgentTypes.GYRO);
                break;
            case AgentTypes.SERIAL:
                agent = buildSerial(a, q);
                break;
            case AgentTypes.CAN_SERIAL:
                agent = buildCANSerial(a, q);
                break;
            case AgentTypes.CAN_SOCKET:
                agent = buildCANSocket(a, q);
                break;
            case AgentTypes.TCP:
                agent = buildSocket(a, q);
                break;
            case AgentTypes.JSON:
                agent = buildSocketJSON(a, q);
                break;
            case AgentTypes.UDP:
                agent = buildUDP(a, q);
                break;
            case AgentTypes.CONSOLE:
                agent = buildStandard(a, q, NMEAConsoleTarget.class, AgentTypes.CONSOLE);
                break;
            case AgentTypes.SEATALK_ALARMS:
                agent = buildStandard(a, q, NMEASeatalkAlarmAgentImpl.class, AgentTypes.SEATALK_ALARMS);
                break;
            case AgentTypes.TRACK:
                agent = buildTrackTarget(a, q);
                break;
            case AgentTypes.METEO:
                agent = buildStandard(a, q, NMEAMetricDBTarget.class, AgentTypes.METEO);
                break;
            case AgentTypes.POWER:
                agent = buildStandard(a, q, NMEAPowerDBTarget.class, AgentTypes.POWER);
                break;
            case AgentTypes.METEO_MON:
                agent = buildStandard(a, q, NMEAMeteoMonitorTarget.class, AgentTypes.METEO_MON);
                break;
            case AgentTypes.GPX_PLAYER:
                agent = buildGPXPlayer(a, q);
                break;
            case AgentTypes.PLAYER:
                agent = buildPlayer(a, q);
                break;
            case AgentTypes.VOLT:
                agent = buildStandard(a, q, NMEAVoltageSensor.class, AgentTypes.VOLT);
                break;
            case AgentTypes.GPS:
                agent = buildGPSStatus(a, q);
                break;
            case AgentTypes.AIS:
                agent = buildStandard(a, q, NMEAAISAgent.class, AgentTypes.AIS);
                break;
            default:
                break;
        }
        return agent;
    }

    private NMEAAgent buildCANSocket(AgentConfJSON a, QOS q) {
        String name = a.getName();
        String device = getString(a.getConfiguration(), DEVICE_ATTRIBUTE, "vcan0");

        NMEACANBusSocketAgent socketAgent = ThingsFactory.getInstance(NMEACANBusSocketAgent.class);
        socketAgent.setup(name, q, device);
        return socketAgent;
    }

    private NMEAAgent buildPlayer(AgentConfJSON a, QOS q) {
        NMEAPlayer play = buildStandard(a, q, NMEAPlayer.class, AgentTypes.PLAYER);
        if (play != null) {
            String playFile = a.getConfiguration().getString("file");
            play.setFile(playFile);
        }
        return play;
    }

    private NMEAAgent buildCANSerial(AgentConfJSON a, QOS q) {
        String name = a.getName();
        String portName = getString(a.getConfiguration(), DEVICE_ATTRIBUTE, "/dev/ttyUSB0");
        int speed = getInt(a.getConfiguration(), "bps", 115200);

        NMEACANBusSerialAgent serial = ThingsFactory.getInstance(NMEACANBusSerialAgent.class);
        serial.setup(name, q, portName, speed);
        return serial;
    }

    private NMEAAgent buildNextion(AgentConfJSON a, QOS q) {
        if (a.getConfiguration().has(PORT_ATTRIBUTE)) {
            NextionDisplayAgent nx = null;
            try {
                nx = ThingsFactory.getInstance(NextionDisplayAgent.class);
                String src = extractSource(a);
                nx.setup(a.getName(), a.getConfiguration().getString(PORT_ATTRIBUTE), src, q);
            } catch (Exception e) {
                log.errorForceStacktrace(() -> LogStringBuilder.start(AGENT_BUILDER_CATEGORY).wO(BUILD_AGENT_KEY_NAME).wV("type", AgentTypes.NEXTION).toString(), e);
            }
            return nx;
        } else {
            return null;
        }
    }

    private NMEAAgent buildGPSStatus(AgentConfJSON a, QOS q) {
        NMEAGPSStatusAgent gps = null;
        try {
            gps = ThingsFactory.getInstance(NMEAGPSStatusAgent.class);
            gps.setup(a.getName(), q);
        } catch (Exception e) {
            log.errorForceStacktrace(() -> LogStringBuilder.start(AGENT_BUILDER_CATEGORY).wO(BUILD_AGENT_KEY_NAME).wV("type", AgentTypes.GPS).toString(), e);
        }
        return gps;
    }

    private NMEAAgent buildGPXPlayer(AgentConfJSON g, QOS q) {
        if (g.getConfiguration().has("file")) {
            NMEAGPXPlayerAgent gpx = null;
            try {
                String file = g.getConfiguration().getString("file");
                gpx = ThingsFactory.getInstance(NMEAGPXPlayerAgent.class);
                gpx.setup(g.getName(), q);
                gpx.setFile(file);
            } catch (Exception e) {
                log.errorForceStacktrace(() -> LogStringBuilder.start(AGENT_BUILDER_CATEGORY).wO(BUILD_AGENT_KEY_NAME).wV("type", AgentTypes.GPX_PLAYER).toString(), e);
            }
            return gpx;
        } else {
            return null;
        }
    }

    private NMEAAgent buildSerial(AgentConfJSON s, QOS q) {
        String name = s.getName();
        String portName = getString(s.getConfiguration(), DEVICE_ATTRIBUTE, "/dev/ttyUSB0");
        int speed = getInt(s.getConfiguration(), "bps", 9600);
        boolean t;
        boolean r;
        switch (s.getInOut()) {
            case IN:
                r = true;
                t = false;
                break;
            case OUT:
                r = false;
                t = true;
                break;
            case INOUT:
                r = true;
                t = true;
                break;
            default:
                r = false;
                t = false;
                break;
        }

        NMEASerial serial = ThingsFactory.getInstance(NMEASerial.class);
        serial.setup(name, portName, speed, r, t, q);
        return serial;
    }

    private NMEAAgent buildUDP(AgentConfJSON conf, QOS q) {
        int port = getInt(conf.getConfiguration(), PORT_ATTRIBUTE, 1222);
        if (conf.getInOut() == InOut.OUT) {
            NMEAUDPSender a = ThingsFactory.getInstance(NMEAUDPSender.class);
            a.setup(conf.getName(), q, port);
            String[] addresses = conf.getConfiguration().getString("addresses").split(",");
            for (String s : addresses) {
                a.addTarget(s);
            }
            return a;
        } else {
            NMEAUDPReceiver a = ThingsFactory.getInstance(NMEAUDPReceiver.class);
            a.setup(conf.getName(), q, port);
            return a;
        }
    }

    private NMEAAgent buildSocketJSON(AgentConfJSON s, QOS q) {
        String name = s.getName();
        int port = getInt(s.getConfiguration(), PORT_ATTRIBUTE, 1113);
        NMEASocketServer c = ThingsFactory.getInstance(NMEASocketServer.class);
        c.setup(name, q, new NetConf(null, port, false, true),
                new NMEASocketServer.SentenceSerializer() {
                    final NMEA2JSONb js = new NMEA2JSONb();

                    @Override
                    public String getOutSentence(Sentence s) {
                        return js.convert(s).toString();
                    }
                });
        return c;
    }

    private NMEAAgent buildSocket(AgentConfJSON s, QOS q) {
        if (getString(s.getConfiguration(), HOST_ATTRIBUTE, null) == null) {
            return buildServerSocket(s, q);
        } else {
            return buildClientSocket(s, q);
        }
    }

    private NMEAAgent buildClientSocket(AgentConfJSON s, QOS q) {
        String server = s.getConfiguration().getString(HOST_ATTRIBUTE);
        String name = s.getName();
        int port = getInt(s.getConfiguration(), PORT_ATTRIBUTE, 1111);
        boolean t;
        boolean r;
        switch (s.getInOut()) {
            case IN:
                r = true;
                t = false;
                break;
            case OUT:
                r = false;
                t = true;
                break;
            case INOUT:
                r = true;
                t = true;
                break;
            default:
                r = false;
                t = false;
                break;
        }
        NMEASocketClient c = ThingsFactory.getInstance(NMEASocketClient.class);
        c.setup(name, q, new NetConf(server, port, r, t));
        return c;
    }

    private static int getInt(JSONObject conf, String attr, int def) {
        try {
            return conf.getInt(attr);
        } catch (Exception ignore) {
            // ignore, not important
        }
        return def;
    }

    private static String getString(JSONObject conf, String attr, String def) {
        try {
            return conf.getString(attr);
        } catch (Exception ignore) {
            // ignore, not important
        }
        return def;
    }

    private NMEAAgent buildServerSocket(AgentConfJSON s, QOS q) {
        String name = s.getName();
        int port = getInt(s.getConfiguration(), PORT_ATTRIBUTE, 1111);
        boolean t;
        boolean r;
        switch (s.getInOut()) {
            case IN:
                r = true;
                t = false;
                break;
            case OUT:
                r = false;
                t = true;
                break;
            case INOUT:
                r = true;
                t = true;
                break;
            default:
                r = false;
                t = false;
                break;
        }
        NMEASocketServer c = ThingsFactory.getInstance(NMEASocketServer.class);
        c.setup(name, q, new NetConf(null, port, r, t), Sentence::toSentence);
        return c;
    }

    private NMEAAgent buildTrackTarget(AgentConfJSON c, QOS q) {
        NMEATrackAgent track = ThingsFactory.getInstance(NMEATrackAgent.class);
        track.setup(c.getName(), q);
        if (c.getConfiguration().has("period")) track.setPeriod(c.getConfiguration().getInt("period") * 1000L);
        if (c.getConfiguration().has("periodStationary"))
            track.setPeriod(c.getConfiguration().getInt("periodStationary") * 1000L);
        return track;
    }

    private <T extends NMEAAgent> T buildStandard(AgentConfJSON s, QOS q, Class<T> c, String type) {
        T a = null;
        try {
            a = ThingsFactory.getInstance(c);
            a.setup(s.getName(), q);
        } catch (Exception e) {
            log.errorForceStacktrace(() -> LogStringBuilder.start(AGENT_BUILDER_CATEGORY).wO(BUILD_AGENT_KEY_NAME).wV("type", type).toString(), e);
        }
        return a;
    }

    private NMEAAgent buildStreamDump() {
        QOS q = createBuiltInQOS();
        NMEA2FileAgent dumper = ThingsFactory.getInstance(NMEA2FileAgent.class);
        dumper.setup("Log", q);
        return dumper;
    }

    private NMEAAgent buildDPTStats() {
        QOS q = createBuiltInQOS();
        DepthStatsAgent a = ThingsFactory.getInstance(DepthStatsAgent.class);
        a.setup("Depth", q);
        return a;
    }

    private NMEAAgent buildPowerLedTarget() {
        if (System.getProperty("os.arch").startsWith("arm")) {
            QOS q = createBuiltInQOS();
            q.addProp(QOSKeys.CANNOT_START_STOP);
            PowerLedAgent pwrLed = ThingsFactory.getInstance(PowerLedAgent.class);
            pwrLed.setup("PowerLed", q);
            return pwrLed;
        } else {
            return null;
        }
    }

    private NMEAAgent buildAutoPilot() {
        QOS q = createBuiltInQOS();
        NMEAAutoPilotAgent ap = ThingsFactory.getInstance(NMEAAutoPilotAgent.class);
        ap.setup("SmartPilot", q);
        return ap;
    }

    private NMEAAgent buildEvoAutoPilot() {
        QOS q = createBuiltInQOS();
        EvoAutoPilotAgent ap = ThingsFactory.getInstance(EvoAutoPilotAgent.class);
        ap.setup("EvoAP", q);
        return ap;
    }

    private NMEAAgent buildFanTarget() {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        FanAgent fan = ThingsFactory.getInstance(FanAgent.class);
        fan.setup("FanManager", q);
        return fan;
    }

    private NMEAAgent buildEngineDetector() {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        EngineDetectionAgentRPM eng = ThingsFactory.getInstance(EngineDetectionAgentRPM.class);
        eng.setup("EngineManager", q);
        return eng;
    }

    private NMEAAgent buildGPSTimeTarget() {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        NMEASystemTimeGPS gpsTime = ThingsFactory.getInstance(NMEASystemTimeGPS.class);
        gpsTime.setup("GPSTime", q);
        return gpsTime;
    }

    public NMEAAgent buildWebUI() {
        QOS q = createBuiltInQOS();
        q.addProp(QOSKeys.CANNOT_START_STOP);
        WebInterfaceAgent web = ThingsFactory.getInstance(WebInterfaceAgent.class);
        web.setup("UI", q);
        return web;
    }

    private QOS createBuiltInQOS() {
        QOS q = new QOS();
        q.addProp(QOSKeys.BUILT_IN);
        return q;
    }

    private static String extractSource(AgentConfJSON a) {
        String src;
        try {
            src = a.getConfiguration().getString("src");
        } catch (Exception e) {
            src = "";
        }
        return src;
    }
}
