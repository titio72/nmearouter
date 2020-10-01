package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.can.N2KHeader;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import net.sf.marineapi.nmea.sentence.Sentence;
import tel.schich.javacan.CanChannels;
import tel.schich.javacan.CanFrame;
import tel.schich.javacan.NetworkDevice;
import tel.schich.javacan.RawCanChannel;
import tel.schich.javacan.linux.LinuxNativeOperationException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.Duration;

import static tel.schich.javacan.CanSocketOptions.SO_RCVTIMEO;

public class NMEACANBusSocketAgent extends NMEAAgentImpl {

    public static final String ERROR_READING_FRAME = "Error reading frame";
    private String netDeviceName;
    private RawCanChannel channel;
    private final N2KFastCache fastCache;
    private final N2KMessage2NMEA0183 converter;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageFactory messageFactory;
    private long lastStats;
    private String description;
    private static final String STATS_TAG = "STATS ";

    private class Stats {
        long messages;
        long messagesAccepted;
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

        void reset() {
            synchronized (this) {
                messagesAccepted = 0;
                messages = 0;
                lastReset = getCache().getNow();
            }
        }

        String toString(long t) {
            return String.format("Message {%d} Accepted {%d} Period {%d}", messages, messagesAccepted, t - lastReset);
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public NMEACANBusSocketAgent(@NotNull NMEACache cache, @NotNull N2KFastCache fastCache,
                                 @NotNull SerialCANReader serialCanReader, @NotNull N2KMessage2NMEA0183 converter,
                                 @NotNull N2KMessageFactory messageFactory) {
        super(cache);
        setSourceTarget(true, false);
        stats.reset();
        this.messageFactory = messageFactory;
        this.fastCache = fastCache;

        fastCache.setCallback(this::onReceive);

        srcFilter = new PGNSourceFilter(getLogger());
        this.converter = converter;
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

    public void setup(String name, QOS qos, String netDevice) {
        super.setup(name, qos);
        netDeviceName = netDevice;
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
            try {
                channel = CanChannels.newRawChannel();
                channel.bind(NetworkDevice.lookup(netDeviceName));
                Duration timeout = Duration.ofMillis(50);
                channel.setOption(SO_RCVTIMEO, timeout);
                Thread t = new Thread(() -> {
                    byte[] data = new byte[32];
                    while (isStarted()) {
                        readFrame(data);
                    }
                });
                t.start();
            } catch (IOException e) {
                getLogger().error("Error creating CAN network channel", e);
                channel = null;
                return false;
            }
            return true;
        }
        return false;
    }

    private void readFrame(byte[] data) {
        try {
            CanFrame frame = channel.read();
            frame.getData(data, 0, frame.getDataLength());
            N2KMessageHeader h = new N2KHeader(frame.getId());
            N2KMessage msg = messageFactory.newInstance(h, data);
            fastCache.onMessage(msg);
        } catch (LinuxNativeOperationException e) {
            if (e.getErrorNumber() != 11) {
                getLogger().error(ERROR_READING_FRAME, e);
            }
        } catch (IOException e) {
            getLogger().error(ERROR_READING_FRAME, e);
        } catch (PGNDataParseException e) {
            getLogger().warning(ERROR_READING_FRAME, e);
        }
    }

    @Override
    protected void onDeactivate() {
        super.onDeactivate();
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                getLogger().error("Error closing CAN network channel", e);
            }
            channel = null;
        }
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
            stats.reset();
            lastStats = t;
        }
    }
}
