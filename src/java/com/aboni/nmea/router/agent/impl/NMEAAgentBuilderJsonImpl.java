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

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentBuilderJson;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.simulator.NMEASimulatorSource;
import com.aboni.nmea.router.conf.InOut;
import com.aboni.nmea.router.conf.AgentTypes;
import com.aboni.nmea.router.conf.ConfJSON;
import com.aboni.nmea.router.conf.net.NetConf;
import com.aboni.nmea.sentences.NMEA2JSONb;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;

public class NMEAAgentBuilderJsonImpl implements NMEAAgentBuilderJson {

    @Inject
    public NMEAAgentBuilderJsonImpl() {
        // do nothing
    }

    @Override
    public NMEAAgent createAgent(ConfJSON.AgentDef a) {
        NMEAAgent agent = null;
        QOS q = a.getQos();
        switch (a.getType()) {
            case AgentTypes.SIMULATOR:
                agent = buildSimulator(a, q);
                break;
            case AgentTypes.SENSOR:
                agent = buildSensor(a, q);
                break;
            case AgentTypes.GYRO:
                agent = buildGyro(a, q);
                break;
            case AgentTypes.SERIAL:
                agent = buildSerial(a, q);
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
                agent = buildConsoleTarget(a, q);
                break;
            case AgentTypes.CONSOLE_N2K:
                agent = buildN2KConsoleTarget(a, q);
                break;
            case AgentTypes.TRACK:
                agent = buildTrackTarget(a, q);
                break;
            case AgentTypes.METEO:
                agent = buildMeteoTarget(a, q);
                break;
            case AgentTypes.MWD:
                agent = buildMWDSynthesizer(q);
                break;
            case AgentTypes.GPX:
                agent = buildGPXPlayer(a, q);
                break;
            case AgentTypes.VOLT:
                agent = buildVoltage(a, q);
                break;
            case AgentTypes.GPS:
                agent = buildGPSStatus(a, q);
                break;
            case AgentTypes.AIS:
                agent = buildAIS(a, q);
                break;
            default:
                break;
        }
        return agent;
    }

    private NMEAAgent buildAIS(ConfJSON.AgentDef a, QOS q) {
        NMEAAISAgent ais = null;
        try {
            ais = ThingsFactory.getInstance(NMEAAISAgent.class);
            ais.setup(a.getName(), q);
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot create GPS Status Agent", e);
        }
        return ais;
    }

    private NMEAAgent buildGPSStatus(ConfJSON.AgentDef a, QOS q) {
        NMEAGPSStatusAgent gps = null;
        try {
            gps = ThingsFactory.getInstance(NMEAGPSStatusAgent.class);
            gps.setup(a.getName(), q);
        } catch (Exception e) {
            ServerLog.getLogger().error("Cannot create GPS Status Agent", e);
        }
        return gps;
    }

    private NMEAAgent buildGPXPlayer(ConfJSON.AgentDef g, QOS q) {
        if (g.getConfiguration().has("file")) {
            NMEAGPXPlayerAgent gpx = null;
            try {
                String file = g.getConfiguration().getString("file");
                gpx = ThingsFactory.getInstance(NMEAGPXPlayerAgent.class);
                gpx.setup(g.getName(), q);
                gpx.setFile(file);
            } catch (Exception e) {
                ServerLog.getLogger().error("Cannot create GPX reader", e);
            }
            return gpx;
        } else {
            return null;
        }
    }

    private NMEAAgent buildConsoleTarget(ConfJSON.AgentDef c, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEAConsoleTarget.class);
        a.setup(c.getName(), q);
        return a;
    }

    private NMEAAgent buildN2KConsoleTarget(ConfJSON.AgentDef c, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEAConsoleN2KTarget.class);
        a.setup(c.getName(), q);
        return a;
    }

    private NMEAAgent buildSerial(ConfJSON.AgentDef s, QOS q) {
        String name = s.getName();
        String portName = getString(s.getConfiguration(), "device", "/dev/ttyUSB0");
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

    private NMEAAgent buildUDP(ConfJSON.AgentDef conf, QOS q) {
        int port = getInt(conf.getConfiguration(), "port", 1222);
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

    private NMEAAgent buildMWDSynthesizer(QOS q) {
        NMEAAgent mwd = ThingsFactory.getInstance(NMEAMWDSentenceCalculator.class);
        mwd.setup("MWD", q);
        return mwd;
    }

    private NMEAAgent buildMeteoTarget(ConfJSON.AgentDef a, QOS q) {
        NMEAAgent meteo = ThingsFactory.getInstance(NMEAMeteoTarget.class);
        meteo.setup(a.getName(), q);
        return meteo;
    }

    private NMEAAgent buildSocketJSON(ConfJSON.AgentDef s, QOS q) {
        String name = s.getName();
        int port = getInt(s.getConfiguration(), "port", 1113);
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

    private NMEAAgent buildSocket(ConfJSON.AgentDef s, QOS q) {
        if (getString(s.getConfiguration(), "host", null) == null) {
            return buildServerSocket(s, q);
        } else {
            return buildClientSocket(s, q);
        }
    }

    private NMEAAgent buildClientSocket(ConfJSON.AgentDef s, QOS q) {
        String server = s.getConfiguration().getString("host");
        String name = s.getName();
        int port = getInt(s.getConfiguration(), "port", 1111);
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

    private NMEAAgent buildServerSocket(ConfJSON.AgentDef s, QOS q) {
        String name = s.getName();
        int port = getInt(s.getConfiguration(), "port", 1111);
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

    private NMEAAgent buildTrackTarget(ConfJSON.AgentDef c, QOS q) {
        NMEATrackAgent track = ThingsFactory.getInstance(NMEATrackAgent.class);
        track.setup(c.getName(), q);
        if (c.getConfiguration().has("period")) track.setPeriod(c.getConfiguration().getInt("period") * 1000L);
        if (c.getConfiguration().has("periodStationary"))
            track.setPeriod(c.getConfiguration().getInt("periodStationary") * 1000L);
        return track;
    }

    private NMEAAgent buildSimulator(ConfJSON.AgentDef s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASimulatorSource.class);
        a.setup(s.getName(), q);
        return a;
    }

    private NMEAAgent buildSensor(ConfJSON.AgentDef s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASourceSensor.class);
        a.setup(s.getName(), q);
        return a;
    }

    private NMEAAgent buildGyro(ConfJSON.AgentDef s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEASourceGyro.class);
        a.setup(s.getName(), q);
        return a;
    }

    private NMEAAgent buildVoltage(ConfJSON.AgentDef s, QOS q) {
        NMEAAgent a = ThingsFactory.getInstance(NMEAVoltageSensor.class);
        a.setup(s.getName(), q);
        return a;
    }
}
