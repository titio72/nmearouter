package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.messages.N2KGNSSPositionUpdate;
import com.aboni.nmea.router.n2k.messages.N2KSOGAdCOGRapid;
import com.aboni.nmea.router.n2k.messages.N2KWaterDepth;
import com.aboni.nmea.sentences.NMEATimestampExtractor;
import com.aboni.sensors.EngineStatus;
import com.fazecast.jSerialComm.SerialPort;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

public class NextionDisplayAgent extends NMEAAgentImpl {

    private static final int PORT_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;
    private static final int PORT_WAIT_FOR_DATA = 500;

    private long lastPortRetryTime;

    private SerialPort port;

    private String portName;
    private final AtomicBoolean run = new AtomicBoolean(false);
    private String logTag = "Nextion";
    private String logError = "Nextion";

    private long lastRMC;
    private String src;
    private long lastInput;

    @Inject
    public NextionDisplayAgent(@NotNull NMEACache cache) {
        super(cache);
    }

    public void setup(String name, String portName, String src, QOS qos) {
        setup(name, qos);
        this.portName = portName;
        this.src = src;
        setSourceTarget(false, true);
        logTag = "Nextion Port {" + portName + "}";
        logError = logTag + " read error {%s}";

    }

    @Override
    public String getType() {
        return "Nextion Display";
    }

    @Override
    public String getDescription() {
        return String.format("Nextion display on %s", portName);
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    protected boolean onActivate() {
        try {
            run.set(true);
            return true;
        } catch (Exception e) {
            getLogger().error(logTag + " error initializing nextion agent {" + port + "}", e);
            port = null;
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        run.set(false);
        try {
            if (port != null && port.isOpen()) {
                port.closePort();
            }
        } catch (Exception e) {
            getLogger().error(logTag + " error closing", e);
        } finally {
            resetPortAndReader();
        }
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = getCache().getNow();
            if ((port == null && Utils.isOlderThan(lastPortRetryTime, now, PORT_OPEN_RETRY_TIMEOUT))) {
                resetPortAndReader();
                getLogger().info(logTag + " creating");
                SerialPort p = SerialPort.getCommPort(portName);
                p.setComPortParameters(9600, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                p.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, PORT_TIMEOUT, PORT_TIMEOUT);
                if (!p.openPort()) {
                    lastPortRetryTime = now;
                } else {
                    lastPortRetryTime = 0;
                    port = p;
                    try {
                        initNextion();
                    } catch (IOException e) {
                        // todo
                    }
                }
            }
            return port;
        }
    }

    private void resetPortAndReader() {
        if (port != null) {
            port.closePort();
        }
        port = null;
    }

    private void initNextion() throws IOException {
        sendCommand("");
        sendCommand("bkcmd=1");
        sendCommand("page 0");
        reset();
    }

    private boolean receiveRetCommandFinished() throws IOException {
        try {
            InputStream is = port.getInputStream();
            int i = 0;
            long now = System.currentTimeMillis();
            byte[] res = new byte[4];
            while (/*(System.currentTimeMillis()-now)<PORT_WAIT_FOR_DATA && */i < 4) {
                int r = is.read();
                if (r != -1) {
                    res[i] = (byte) r;
                    i++;
                }
            }
            return res[0] == 1;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean sendCommand(@NotNull String cmd) throws IOException {
        if (getPort() != null) {
            byte cmdTerm[] = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            port.getOutputStream().write(cmd.getBytes());
            port.getOutputStream().write(cmdTerm);
            return receiveRetCommandFinished();
        } else return false;
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (src.isEmpty() || src.equals(source)) {
            lastInput = getCache().getNow();
        }
        try {
            if (s instanceof RMCSentence) {
                sendPosition(((RMCSentence) s).getPosition());
                try {
                    sendCommand(String.format("cog.txt=\"%d°\"", Math.round(((RMCSentence) s).getCourse())));
                } catch (DataNotAvailableException ee) {
                    sendCommand("cog.txt=\"\"");
                }
                try {
                    sendCommand(String.format("sog.txt=\"%.1f Kn\"", ((RMCSentence) s).getSpeed()));
                } catch (DataNotAvailableException ee) {
                    sendCommand("sog.txt=\"\"");
                }
            } else if (s instanceof DPTSentence) {
                try {
                    sendCommand(String.format("depth.txt=\"%.1f m\"", ((DPTSentence) s).getDepth()));
                } catch (DataNotAvailableException ee) {
                    sendCommand("depth.txt=\"\"");
                }
            } else if (s instanceof MMBSentence) {
                try {
                    sendCommand(String.format("atmo.txt=\"%d mb\"", Math.round(((MMBSentence) s).getBars() * 1000)));
                } catch (DataNotAvailableException ee) {
                    sendCommand("atmo.txt=\"\"");
                }
            } else if (s instanceof MTWSentence) {
                try {
                    sendCommand(String.format("wtemp.txt=\"%.1f C°\"", ((MTWSentence) s).getTemperature()));
                } catch (DataNotAvailableException ee) {
                    sendCommand("wtemp.txt=\"\"");
                }
            } else if (s instanceof MTASentence) {
                try {
                    sendCommand(String.format("atemp.txt=\"%.1f C°\"", ((MTASentence) s).getTemperature()));
                } catch (DataNotAvailableException ee) {
                    sendCommand("atemp.txt=\"\"");
                }
            } else if (s instanceof MHUSentence) {
                try {
                    sendCommand(String.format("hum.txt=\"%.1f%%\"", ((MHUSentence) s).getRelativeHumidity()));
                } catch (DataNotAvailableException ee) {
                    sendCommand("hum.txt=\"\"");
                }
            } else if (s instanceof HDMSentence) {
                try {
                    sendCommand(String.format("head.txt=\"%d°\"", Math.round(((HDMSentence) s).getHeading())));
                } catch (DataNotAvailableException ee) {
                    sendCommand("head.txt=\"\"");
                }
            }
        } catch (IOException e) {
            // TODO
        }
    }

    private void reset() throws IOException {
        sendCommand("hum.txt=\"\"");
        sendCommand("atmo.txt=\"\"");
        sendCommand("wtemp.txt=\"\"");
        sendCommand("atemp.txt=\"\"");
        sendCommand("cog.txt=\"\"");
        sendCommand("sog.txt=\"\"");
        sendCommand("lon.txt=\"\"");
        sendCommand("lat.txt=\"\"");
        sendCommand("depth.txt=\"\"");
        sendCommand("head.txt=\"\"");
        sendCommand("time.txt=\"\"");
    }

    private void sendPosition(Position p) throws IOException {
        if (p != null) {
            lastRMC = getCache().getNow();
            sendCommand(String.format("lat.txt=\"%s\"", Utils.formatLatitude(p.getLatitude())));
            sendCommand(String.format("lon.txt=\"%s\"", Utils.formatLatitude(p.getLongitude())));
        } else {
            sendCommand("lat.txt=\"\"");
            sendCommand("lon.txt=\"\"");
        }
    }

    //@OnN2KMessage
    public void onMessage(N2KMessage msg) {
        try {
            if (msg instanceof N2KGNSSPositionUpdate) {
                sendPosition(((N2KGNSSPositionUpdate) msg).getPosition());
            } else if (msg instanceof N2KSOGAdCOGRapid) {
                if (!Double.isNaN(((N2KSOGAdCOGRapid) msg).getSOG())) {
                    sendCommand(String.format("sog.txt=\"%.1f Kn\"", ((N2KSOGAdCOGRapid) msg).getSOG()));
                } else {
                    sendCommand("sog.txt=\"\"");
                }
                if (!Double.isNaN(((N2KSOGAdCOGRapid) msg).getCOG())) {
                    sendCommand(String.format("cog.txt=\"%d°\"", Math.round(((N2KSOGAdCOGRapid) msg).getCOG())));
                } else {
                    sendCommand("cog.txt=\"\"");
                }
            } else if (msg instanceof N2KWaterDepth) {
                if (!Double.isNaN(((N2KWaterDepth) msg).getDepth())) {
                    sendCommand(String.format("depth.txt=\"%.1f m\"", ((N2KWaterDepth) msg).getDepth()));
                } else {
                    sendCommand("depth.txt=\"\"");
                }
            }
        } catch (IOException e) {
            // TODO
        }
    }

    @Override
    public void onTimer() {
        super.onTimer();
        try {
            try {
                ZonedDateTime dt = ZonedDateTime.now();
                DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
                sendCommand(String.format("time.txt=\"%s\"", f.format(dt)));
            } catch (Exception e) {
                sendCommand("time.txt=\"\"");
            }

            boolean engine = getCache().getStatus(NMEARouterStatuses.ENGINE_STATUS, EngineStatus.UNKNOWN) == EngineStatus.ON;
            sendCommand(String.format("engine.pic=%d", engine ? 2 : 1));
            boolean anchor = getCache().getStatus(NMEARouterStatuses.ANCHOR_STATUS, Boolean.FALSE);
            sendCommand(String.format("nav.pic=%d", anchor ? 0 : 3));

            boolean gps = (getCache().getNow() - lastRMC) < 10000;
            sendCommand(String.format("gps.pic=%d", gps ? 5 : 4));

            boolean net = (getCache().getNow() - lastInput) < 2000;
            sendCommand(String.format("nmea.pic=%d", net ? 7 : 6));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
