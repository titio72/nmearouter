package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterStatuses;
import com.aboni.nmea.router.OnSentence;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ServerLog;
import com.fazecast.jSerialComm.SerialPort;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

public class NextionDisplayAgent extends NMEAAgentImpl {

    private static final boolean HANDLE_INPUT = false;
    private static final boolean CONSOLE_OUT = true;

    private static final int PORT_SPEED = 115200;
    private static final int PORT_READ_TIMEOUT = 1000;
    private static final int PORT_WRITE_TIMEOUT = 1000;
    private static final int PORT_OPEN_RETRY_TIMEOUT = 5000;
    private static final int CLEAN_TIMEOUT = 10000;
    private static final int DIM_TIMEOUT = 3000;
    private static final int GPS_TIMEOUT = 10000;
    private static final int INPUT_TIMEOUT = 2000;
    private static final int N_FIELDS = 11;

    private static final class Fields {
        private static final int LAT = 0;
        private static final int LON = 1;
        private static final int SOG = 2;
        private static final int COG = 3;
        private static final int DEPTH = 4;
        private static final int WATER_TEMP = 5;
        private static final int AIR_TEMP = 6;
        private static final int HEAD = 7;
        private static final int HUM = 8;
        private static final int ATMO = 9;
        private static final int TIME = 10;
    }

    private static final String[] FIELDS = new String[]{
            "lat", "lon", "sog", "cog", "depth",
            "wtemp", "atemp", "head", "hum", "atmo",
            "time"
    };

    private static final byte[] CMD_TERM = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    private final AtomicBoolean run = new AtomicBoolean(false);

    private final Dimmer dimmer = new Dimmer();
    private final NextionReader reader = new NextionReader();

    private long lastPortRetryTime;
    private long lastInput;
    private final long[] lastTime = new long[N_FIELDS];

    private String portName;
    private SerialPort port;

    private String src;

    @Inject
    public NextionDisplayAgent(@NotNull NMEACache cache) {
        super(cache);
    }

    public void setup(String name, String portName, String src, QOS qos) {
        setup(name, qos);
        this.portName = portName;
        this.src = src;
        setSourceTarget(false, true);
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
        run.set(true);
        if (HANDLE_INPUT) {
            reader.startReader(this::onInput);
        }
        return true;
    }

    @Override
    protected void onDeactivate() {
        run.set(false);
        if (!HANDLE_INPUT) {
            resetPortAndDisplay();
        }
    }

    private SerialPort getPort() {
        synchronized (this) {
            long now = getCache().getNow();
            if ((port == null && Utils.isOlderThan(lastPortRetryTime, now, PORT_OPEN_RETRY_TIMEOUT))) {
                getLogger().info("Nextion Display opening port {" + portName + "}");
                SerialPort p = SerialPort.getCommPort(portName);
                p.setComPortParameters(PORT_SPEED, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
                p.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_BLOCKING,
                        PORT_READ_TIMEOUT, PORT_WRITE_TIMEOUT);
                if (!p.openPort()) {
                    lastPortRetryTime = now;
                } else {
                    lastPortRetryTime = 0;
                    port = p;
                    initNextion();
                }
            }
            return port;
        }
    }

    private void resetPortAndDisplay() {
        if (port != null) {
            reset();
            getLogger().info("Nextion display closing serial port {" + portName + "}");
            port.closePort();
            port = null;
        }
    }

    private void initNextion() {
        sendCommand("");
        sendCommand("bkcmd=1");
        sendCommand("page 0");
        reset();
    }

    private void sendCommand(@NotNull String cmd) {
        if (getPort() != null) {

            port.writeBytes(cmd.getBytes(), cmd.getBytes().length);
            port.writeBytes(CMD_TERM, 3);
        }
    }

    private interface ExtractString {
        String getValue(Sentence s);
    }

    private void setData(Sentence s, int field, ExtractString e) {
        try {
            sendCommand(FIELDS[field] + ".txt=\"" + e.getValue(s) + "\"");
            lastTime[field] = getCache().getNow();
        } catch (DataNotAvailableException ee) {
            reset(field);
        }
    }

    @OnSentence
    public void onSentence(Sentence s, String source) {
        if (!run.get()) return;

        if (src.isEmpty() || src.equals(source)) {
            lastInput = getCache().getNow();
        }

        if (s instanceof RMCSentence) {
            sendPosition(((RMCSentence) s).getPosition());
            setData(s, Fields.COG, (Sentence ss) -> String.format("%d°", Math.round(((RMCSentence) ss).getCourse())));
            setData(s, Fields.SOG, (Sentence ss) -> String.format("%.1f Kn", ((RMCSentence) ss).getSpeed()));
        } else if (s instanceof DPTSentence) {
            setData(s, Fields.DEPTH, (Sentence ss) -> String.format("%.1f m", ((DPTSentence) ss).getDepth()));
        } else if (s instanceof MMBSentence) {
            setData(s, Fields.ATMO, (Sentence ss) -> String.format("%d mb", Math.round(((MMBSentence) ss).getBars() * 1000)));
        } else if (s instanceof MTWSentence) {
            setData(s, Fields.WATER_TEMP, (Sentence ss) -> String.format("%.1f C°", ((MTWSentence) ss).getTemperature()));
        } else if (s instanceof MTASentence) {
            setData(s, Fields.AIR_TEMP, (Sentence ss) -> String.format("%.1f C°", ((MTASentence) ss).getTemperature()));
        } else if (s instanceof MHUSentence) {
            setData(s, Fields.HUM, (Sentence ss) -> String.format("%.1f%%", ((MHUSentence) ss).getRelativeHumidity()));
        } else if (s instanceof HDMSentence) {
            setData(s, Fields.HEAD, (Sentence ss) -> String.format("%d°", Math.round(((HDMSentence) ss).getHeading())));
        }
    }

    private void reset(int field) {
        sendCommand(FIELDS[field] + ".txt=\"\"");
    }

    private void reset() {
        for (int i = 0; i < N_FIELDS; i++) reset(i);
    }

    private void sendPosition(final Position p) {
        if (p != null) {
            setData(null, Fields.LAT, (Sentence s) -> Utils.formatLatitude(p.getLatitude()));
            setData(null, Fields.LON, (Sentence s) -> Utils.formatLongitude(p.getLongitude()));
        } else {
            reset(Fields.LAT);
            reset(Fields.LON);
        }
    }

    private static String dump(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte bb : b) sb.append(String.format(" %x", bb));
        return sb.toString();
    }

    private void onInput(byte[] b) {
        if (CONSOLE_OUT) {
            ServerLog.getConsoleOut().println("Nextion input:" + dump(b));
        }
        if (isDisplayTouched(b)) {
            dimmer.lightUp(getCache().getNow());
        }
    }

    private static boolean isDisplayTouched(byte[] b) {
        // check return codes for user touch (0x65), page 0 and ID of the hotspot (0x10)
        return b[0] == 0x65 && b[1] == 0x00 && b[2] == 0x10;
    }

    @Override
    public void onTimer() {
        super.onTimer();

        if (!run.get()) return;

        long now = getCache().getNow();

        final ZonedDateTime dt = ZonedDateTime.now();
        final DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MMM HH:mm:ss");
        setData(null, Fields.TIME, (Sentence s) -> String.format("%s", f.format(dt)));

        boolean engine = getCache().getStatus(NMEARouterStatuses.ENGINE_STATUS, EngineStatus.UNKNOWN) == EngineStatus.ON;
        sendCommand(String.format("engine.pic=%d", engine ? 5 : 3));

        Boolean anchor = getCache().getStatus(NMEARouterStatuses.ANCHOR_STATUS, null);
        int anchorIcon;
        if (anchor == null) anchorIcon = 0;
        else if (Boolean.TRUE.equals(anchor)) anchorIcon = 1;
        else anchorIcon = 13;
        sendCommand(String.format("nav.pic=%d", anchorIcon));

        boolean gps = !Utils.isOlderThan(lastTime[Fields.LAT], now, GPS_TIMEOUT);
        sendCommand(String.format("gps.pic=%d", gps ? 7 : 6));

        boolean net = !Utils.isOlderThan(lastInput, now, INPUT_TIMEOUT);
        sendCommand(String.format("nmea.pic=%d", net ? 10 : 9));

        for (int i = 0; i < N_FIELDS; i++) {
            if (Utils.isOlderThan(lastTime[i], now, CLEAN_TIMEOUT)) reset(i);
        }

        dimmer.onTimer(now);
    }

    private interface InputHandler {
        void onInput(byte[] input);
    }

    private class NextionReader {

        private InputHandler handler;

        private void startReader(InputHandler handler) {
            this.handler = handler;
            Thread thread = new Thread(() -> {
                byte[] readBuffer = new byte[32];
                int bfIx = 0;
                while (run.get()) {
                    SerialPort p = getPort();
                    int bytesRead;
                    if (p != null) {
                        do {
                            bytesRead = getPort().readBytes(readBuffer, 1, bfIx);
                            if (bytesRead > 0) {
                                bfIx += bytesRead;
                                bfIx = handleInput(readBuffer, bfIx);
                            }
                        } while (bytesRead > 0);
                        Utils.pause(250);
                    } else {
                        Utils.pause(1000);
                    }
                }
                resetPortAndDisplay();
            });
            thread.start();
        }

        private int handleInput(byte[] readBuffer, int bfIx) {
            if (isTerminated(bfIx, readBuffer)) {
                byte[] b = new byte[bfIx];
                System.arraycopy(readBuffer, 0, b, 0, bfIx - 1);
                if (handler != null) handler.onInput(b);
                bfIx = 0;
            }
            return bfIx;
        }

        private boolean isTerminated(int len, byte[] b) {
            if (len >= CMD_TERM.length) {
                for (int i = 1; i <= CMD_TERM.length; i++) {
                    if (CMD_TERM[CMD_TERM.length - i] != b[b.length - i]) return false;
                }
                return true;
            } else {
                return false;
            }
        }
    }

    private class Dimmer {
        private long lastDim;

        void onTimer(long now) {
            if (lastDim != 0 && Utils.isOlderThan(lastDim, now, DIM_TIMEOUT)) {
                dimDown();
                lastDim = 0;
            }
        }

        void lightUp(long now) {
            lastDim = now;
            dim(100);
        }

        void dimDown() {
            dim(20);
        }

        void dim(int i) {
            sendCommand("dim=" + i);
        }
    }
}
