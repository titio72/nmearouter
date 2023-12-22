package com.aboni.nmea.router.agent.impl;

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.nmea.n2k.N2KFastCache;
import com.aboni.nmea.n2k.N2KMessage;
import com.aboni.nmea.n2k.PGNSourceFilter;
import com.aboni.nmea.n2k.can.SerialCANReader;
import com.aboni.nmea.n2k.messages.N2KMessageFactory;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.message.PositionAndVectorStream;
import com.aboni.nmea.router.message.SpeedAndHeadingStream;
import com.aboni.nmea.router.utils.SerialReader;
import com.aboni.utils.TimestampProvider;
import com.aboni.utils.Utils;

import javax.inject.Inject;

public class NMEACANBusSerialAgent extends NMEAAgentImpl {

    public static final String STATS_KEY_NAME = "stats";
    private final SerialReader serialReader;
    private final SerialCANReader serialCanReader;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageFactory messageFactory;
    private final PositionAndVectorStream posAndVectorStream;
    private final SpeedAndHeadingStream speedAndHeadingStream;
    private long lastStats;
    private String description;

    private class Stats {
        long messages;
        long messagesAccepted;
        long errors;
        long lastReset;

        void incrementMessages() {
            synchronized (this) {
                messages++;
            }
        }

        void incrementAccepted() {
            synchronized (this) {
                messagesAccepted++;
            }
        }

        void incrementErrors() {
            synchronized (this) {
                errors++;
            }
        }

        void reset() {
            synchronized (this) {
                messagesAccepted = 0;
                messages = 0;
                errors = 0;
                lastReset = getTimestampProvider().getNow();
            }
        }

        String toString(long t) {
            return String.format("Message {%d} Accepted {%d} Errors {%s} Period {%d}", messages, messagesAccepted, errors, t - lastReset);
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public NMEACANBusSerialAgent(Log log, TimestampProvider tp, N2KFastCache fastCache,
                                 SerialCANReader serialCanReader,
                                 RouterMessageFactory messageFactory,
                                 N2KMessageFactory msgFactory,
                                 PGNSourceFilter srcFilter) {
        super(log, tp, messageFactory, true, false);
        this.messageFactory = msgFactory;
        this.serialReader = new SerialReader(tp, log);
        this.serialCanReader = serialCanReader;
        this.srcFilter = srcFilter;
        this.posAndVectorStream = new PositionAndVectorStream(tp);
        this.speedAndHeadingStream = new SpeedAndHeadingStream(tp);
        this.posAndVectorStream.setListener(this::postMessage);
        this.speedAndHeadingStream.setListener(this::postMessage);

        stats.reset();
        fastCache.setCallback(this::onReceive);
        serialCanReader.setCallback(fastCache::onMessage);
        serialCanReader.setErrCallback(this::onError);
    }

    private void onError(byte[] buffer, String errorMessage) {
        LogStringBuilder lb = getLogBuilder().wO("read");
        StringBuilder builder = new StringBuilder();
        if (buffer != null) {
            for (byte b : buffer) builder.append(String.format(" %02x", b));
        }
        getLog().error(() -> lb.wV("buffer", builder.toString()).wV("error", errorMessage).toString());
        stats.incrementErrors();
    }

    private void onReceive(N2KMessage msg) {
        assert msg!=null;
        stats.incrementMessages();
        long now = getTimestampProvider().getNow();
        if (srcFilter.accept(msg.getHeader().getSource(), msg.getHeader().getPgn(), now)
                && messageFactory.isSupported(msg.getHeader().getPgn())) {
            srcFilter.setPGNTimestamp(msg.getHeader().getSource(), msg.getHeader().getPgn(), now);
            stats.incrementAccepted();
            posAndVectorStream.onMessage(msg);
            speedAndHeadingStream.onMessage(msg);
            postMessage(msg);
        }
    }

    public void setup(String name, QOS qos, String port, int speed) {
        super.setup(name, qos);
        serialReader.setup("CANBus Serial [" + name + "]", port, speed, serialCanReader::onRead);
    }

    @Override
    public String getType() {
        return "CAN Bus N2K Receiver";
    }

    @Override
    public String getDescription() {
        synchronized (this) {
            return description;
        }
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected boolean onActivate() {
        if (super.onActivate()) {
            serialReader.activate();
            return true;
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        super.onDeactivate();
        serialReader.deactivate();
    }

    @Override
    public void onTimer() {
        super.onTimer();
        if (isStarted()) {
            long t = getTimestampProvider().getNow();
            if ((Utils.isOlderThan(lastStats, t, 30000))) {
                synchronized (this) {
                    description = getType() + " " + stats.toString(t);
                }

                getLog().info(() -> getLogBuilder().wO(STATS_KEY_NAME).w(" " + stats.toString(t)).toString());
                getLog().info(() -> getLogBuilder().wO(STATS_KEY_NAME).w(" " + serialCanReader.getStats().toString(t)).toString());
                getLog().info(() -> getLogBuilder().wO(STATS_KEY_NAME).w(" " + serialReader.getStats().toString(t)).toString());

                stats.reset();
                serialCanReader.getStats().reset(t);
                serialReader.getStats().reset();

                lastStats = t;
            }
        }
    }
}
