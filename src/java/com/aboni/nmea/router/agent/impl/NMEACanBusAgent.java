package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.PGNSourceFilter;
import com.aboni.nmea.router.n2k.can.N2KCanReader;
import com.aboni.nmea.router.n2k.can.N2KFastCache;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefinitions;
import com.aboni.utils.SerialReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEACanBusAgent extends NMEAAgentImpl {

    private final SerialReader serialReader;
    private final N2KCanReader canReader;
    private final N2KMessage2NMEA0183 converter;
    private final PGNSourceFilter srcFilter;
    private long lastStats;

    private class Stats {
        long messages;
        long messagesAccepted;
        long errors;
        long lastReset;

        void incrMessages() {
            synchronized (this) {
                messages++;
            }
        }

        void incrAccepted() {
            synchronized (this) {
                messagesAccepted++;
            }
        }

        void incrErrors() {
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
    public NMEACanBusAgent(@NotNull NMEACache cache, @NotNull N2KFastCache fastCache, @NotNull N2KCanReader canReader, N2KMessage2NMEA0183 converter) {
        super(cache);
        setSourceTarget(true, false);
        stats.reset();

        serialReader = new SerialReader();

        fastCache.setCallback(this::onReceive);

        this.canReader = canReader;
        canReader.setCallback(fastCache::onMessage);
        canReader.setErrCallback(this::onError);

        srcFilter = new PGNSourceFilter(getLogger());
        this.converter = converter;
    }

    private void onError(byte[] buffer) {
        stats.incrErrors();
        StringBuilder sb = new StringBuilder("NMEACanBusAgent Error decoding buffer {");
        for (byte b : buffer) {
            sb.append(String.format(" %02x", b));
        }
        sb.append("}");
        getLogger().error(sb.toString());
    }

    private void onReceive(@NotNull N2KMessage msg) {
        stats.incrMessages();
        if (srcFilter.accept(msg.getHeader().getSource(), msg.getHeader().getPgn())
                && N2KMessageDefinitions.isSupported(msg.getHeader().getPgn())) {
            stats.incrAccepted();
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
        serialReader.setup(port, speed, canReader::onRead);
    }

    @Override
    public String getType() {
        return "CAN Bus N2K Receiver";
    }

    @Override
    public String getDescription() {
        return getType() + " " + stats.toString();
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
            getLogger().info("CAN Agent STATS " + stats.toString(t));
            getLogger().info("CAN Agent STATS " + canReader.getStats().toString(t));
            getLogger().info("CAN Agent STATS " + serialReader.getStats().toString(t));

            stats.reset();
            canReader.getStats().reset();
            serialReader.getStats().reset();

            lastStats = t;
        }
    }
}
