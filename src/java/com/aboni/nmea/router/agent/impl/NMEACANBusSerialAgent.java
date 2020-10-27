package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.message.PositionAndVectorStream;
import com.aboni.nmea.router.message.SpeedAndHeadingStream;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.PGNSourceFilter;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.SerialReader;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEACANBusSerialAgent extends NMEAAgentImpl {

    public static final String STATS_KEY_NAME = "stats";
    private final SerialReader serialReader;
    private final SerialCANReader serialCanReader;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageFactory messageFactory;
    private final TimestampProvider timestampProvider;
    private final Log log;
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
                lastReset = timestampProvider.getNow();
            }
        }

        String toString(long t) {
            return String.format("Message {%d} Accepted {%d} Errors {%s} Period {%d}", messages, messagesAccepted, errors, t - lastReset);
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public NMEACANBusSerialAgent(@NotNull Log log, @NotNull TimestampProvider tp, @NotNull N2KFastCache fastCache,
                                 @NotNull SerialCANReader serialCanReader,
                                 @NotNull N2KMessageFactory msgFactory) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        this.messageFactory = msgFactory;
        this.serialReader = new SerialReader(tp, log);
        this.serialCanReader = serialCanReader;
        this.srcFilter = new PGNSourceFilter(log);
        this.posAndVectorStream = new PositionAndVectorStream(tp);
        this.speedAndHeadingStream = new SpeedAndHeadingStream(tp);
        this.posAndVectorStream.setListener(this::notify);
        this.speedAndHeadingStream.setListener(this::notify);

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
        lb.wV("buffer", builder.toString()).wV("error", errorMessage).error(log);
        stats.incrementErrors();
    }

    private void onReceive(@NotNull N2KMessage msg) {
        stats.incrementMessages();
        if (srcFilter.accept(msg.getHeader().getSource(), msg.getHeader().getPgn())
                && messageFactory.isSupported(msg.getHeader().getPgn())) {
            stats.incrementAccepted();
            posAndVectorStream.onMessage(msg);
            speedAndHeadingStream.onMessage(msg);
            notify(msg);
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
            long t = timestampProvider.getNow();
            if ((Utils.isOlderThan(lastStats, t, 30000))) {
                synchronized (this) {
                    description = getType() + " " + stats.toString(t);
                }

                getLogBuilder().wO(STATS_KEY_NAME).w(" " + stats.toString(t)).info(log);
                getLogBuilder().wO(STATS_KEY_NAME).w(" " + serialCanReader.getStats().toString(t)).info(log);
                getLogBuilder().wO(STATS_KEY_NAME).w(" " + serialReader.getStats().toString(t)).info(log);

                stats.reset();
                serialCanReader.getStats().reset(t);
                serialReader.getStats().reset();

                lastStats = t;
            }
        }
    }
}
