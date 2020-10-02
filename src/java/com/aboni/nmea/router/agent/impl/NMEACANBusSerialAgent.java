package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.PGNSourceFilter;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.SerialReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEACANBusSerialAgent extends NMEAAgentImpl {

    private final SerialReader serialReader;
    private final SerialCANReader serialCanReader;
    private final N2KMessage2NMEA0183 converter;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageFactory messageFactory;
    private long lastStats;
    private String description;
    private static final String STATS_TAG = "STATS ";

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
                lastReset = getCache().getNow();
            }
        }

        String toString(long t) {
            return String.format("Message {%d} Accepted {%d} Errors {%s} Period {%d}", messages, messagesAccepted, errors, t - lastReset);
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public NMEACANBusSerialAgent(@NotNull NMEACache cache, @NotNull N2KFastCache fastCache,
                                 @NotNull SerialCANReader serialCanReader, @NotNull N2KMessage2NMEA0183 converter,
                                 @NotNull N2KMessageFactory msgFactory) {
        super(cache);
        messageFactory = msgFactory;
        setSourceTarget(true, false);
        stats.reset();
        serialReader = new SerialReader(cache, getLogger());
        fastCache.setCallback(this::onReceive);
        this.serialCanReader = serialCanReader;
        serialCanReader.setCallback(fastCache::onMessage);
        serialCanReader.setErrCallback(this::onError);
        srcFilter = new PGNSourceFilter(getLogger());
        this.converter = converter;
    }

    private void onError(byte[] buffer, String errorMessage) {
        getLogger().debug((() -> {
            StringBuilder builder = new StringBuilder("Error reading frame:buffer {");
            if (buffer != null) {
                for (byte b : buffer) builder.append(String.format(" %02x", b));
            }
            builder.append("} error {").append(errorMessage).append("}");
            return builder.toString();
        }));
        stats.incrementErrors();
    }

    private void onReceive(@NotNull N2KMessage msg) {
        stats.incrementMessages();
        if (srcFilter.accept(msg.getHeader().getSource(), msg.getHeader().getPgn())
                && messageFactory.isSupported(msg.getHeader().getPgn())) {
            stats.incrementAccepted();
            notify(msg);
            if (converter != null) {
                Sentence[] s = converter.getSentence(msg);
                if (s != null) {
                    for (Sentence ss : s) notify(ss);
                }
            }
        }
    }

    public void setup(String name, QOS qos, String port, int speed) {
        super.setup(name, qos);
        serialReader.setup(port, speed, serialCanReader::onRead);
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
        long t = getCache().getNow();
        if ((Utils.isOlderThan(lastStats, t, 30000))) {
            synchronized (this) {
                description = getType() + " " + stats.toString(t);
            }

            getLogger().info(STATS_TAG + stats.toString(t));
            getLogger().info(STATS_TAG + serialCanReader.getStats().toString(t));
            getLogger().info(STATS_TAG + serialReader.getStats().toString(t));

            stats.reset();
            serialCanReader.getStats().reset(t);
            serialReader.getStats().reset();

            lastStats = t;
        }
    }
}
