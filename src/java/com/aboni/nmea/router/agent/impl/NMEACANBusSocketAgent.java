package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.can.N2KHeader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.Log;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static tel.schich.javacan.CanSocketOptions.SO_RCVTIMEO;

public class NMEACANBusSocketAgent extends NMEAAgentImpl {

    public static final String ERROR_READING_FRAME = "Error reading frame";
    public static final String ERROR_TYPE_KEY_NAME = "error type";
    public static final String READ_KEY_NAME = "read";
    private final Log log;
    private String netDeviceName;
    private RawCanChannel channel;
    private final N2KFastCache fastCache;
    private final N2KMessage2NMEA0183 converter;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageFactory messageFactory;
    private final TimestampProvider timestampProvider;
    private long lastStats;
    private String description;
    private final AtomicBoolean run = new AtomicBoolean();
    private static final boolean DEBUG = false;

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
                lastReset = timestampProvider.getNow();
            }
        }

        String toString(long t) {
            return String.format("Message {%d} Accepted {%d} Period {%d}", messages, messagesAccepted, t - lastReset);
        }
    }

    private final Stats stats = new Stats();

    @Inject
    public NMEACANBusSocketAgent(@NotNull Log log, @NotNull TimestampProvider tp, @NotNull N2KFastCache fastCache,
                                 @NotNull N2KMessage2NMEA0183 converter, @NotNull N2KMessageFactory messageFactory) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        this.messageFactory = messageFactory;
        this.fastCache = fastCache;
        this.srcFilter = new PGNSourceFilter(log);
        this.converter = converter;
        stats.reset();
        fastCache.setCallback(this::onReceive);
    }

    private void onReceive(@NotNull N2KMessage msg) {
        stats.incrementMessages();
        stats.incrementAccepted();
        notify(msg);
        if (converter != null) {
            Sentence[] s = converter.getSentence(msg);
            if (s != null) {
                for (Sentence ss : s) notify(ss);
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
                run.set(true);
                channel = CanChannels.newRawChannel();
                channel.bind(NetworkDevice.lookup(netDeviceName));
                Duration timeout = Duration.ofMillis(50);
                channel.setOption(SO_RCVTIMEO, timeout);
                Thread t = new Thread(() -> {
                    while (run.get()) {
                        readFrame();
                    }
                });
                t.start();
            } catch (IOException e) {
                run.set(false);
                getLogBuilder().wO("activate").error(log, e);
                channel = null;
                return false;
            }
            return true;
        }
        return false;
    }

    private void readFrame() {
        try {
            CanFrame frame = channel.read();
            byte[] data = new byte[frame.getDataLength()];
            frame.getData(data, 0, frame.getDataLength());
            N2KMessageHeader h = new N2KHeader(frame.getId());
            if (srcFilter.accept(h.getSource(), h.getPgn()) && messageFactory.isSupported(h.getPgn()) && h.getDest()==0xFF) {
                N2KMessage msg = messageFactory.newUntypedInstance(h, data);
                fastCache.onMessage(msg);
            }
        } catch (LinuxNativeOperationException e) {
            if (e.getErrorNumber() != 11) {
                getLogBuilder().wO(READ_KEY_NAME).wV(ERROR_TYPE_KEY_NAME, "native linux error").error(log, e);
            }
        } catch (IOException e) {
            getLogBuilder().wO(READ_KEY_NAME).wV(ERROR_TYPE_KEY_NAME, "IO").error(log, e);
        } catch (Exception e) {
            if (DEBUG) {
                getLogBuilder().wO(READ_KEY_NAME).wV(ERROR_TYPE_KEY_NAME, "unexpected error").error(log, e);
            }
        }
    }

    @Override
    protected void onDeactivate() {
        super.onDeactivate();
        if (channel != null) {
            run.set(false);
            try {
                channel.close();
            } catch (IOException e) {
                getLogBuilder().wO("deactivate").error(log, e);
            }
            channel = null;
        }
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
                getLogBuilder().wO("stats").w(" " + stats.toString(t)).info(log);
                stats.reset();
                lastStats = t;
            }
        }
    }
}
