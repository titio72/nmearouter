package com.aboni.nmea.router.agent.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.n2k.CANBOATStream;
import com.aboni.nmea.router.n2k.CANBOATDecoder;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class NMEAUDPN2KReceiver extends NMEAAgentImpl {

    private static final int FAST_STATS_PERIOD = 1000;
    private static final int STATS_PERIOD = 60000;
    private static final int OPEN_SOCKET_RETRY_TIME = 3000;
    private static final int SOCKET_READ_TIMEOUT = 60000;

    private int port;
    private boolean stop;
    private boolean setup;
    private final CANBOATDecoder decoder;
    private final CANBOATStream n2kStream;
    private final Stats fastStats;
    private final Stats stats;
    private final byte[] buffer = new byte[2048];

    private static class StatsSpeed {
        long bytesIn = 0;
        long resetTime = 0;

        long getBpsIn(long time) {
            return (bytesIn * 1000) / (time - resetTime);
        }

        void reset(long time) {
            bytesIn = 0;
            resetTime = time;
        }
    }

    private static class Stats extends StatsSpeed {
        long sentences = 0;
        long sentenceErrs = 0;

        @Override
        void reset(long time) {
            super.reset(time);
            sentenceErrs = 0;
            sentences = 0;
        }
    }

    @Inject
    public NMEAUDPN2KReceiver(@NotNull NMEACache cache, CANBOATDecoder n2kDecoder) {
        super(cache);
        setSourceTarget(true, false);
        decoder = n2kDecoder;
        n2kStream = new CANBOATStream(getLogger());
        fastStats = new Stats();
        stats = new Stats();
    }

    public void setup(String name, QOS q, int port) {
        if (!setup) {
            setup = true;
            setup(name, q);
            this.port = port;
            getLogger().info(String.format("Setting up UDP receiver: Port {%d}", port));
        } else {
            getLogger().info("Cannot setup UDP receiver - already set up");
        }
    }

    @Override
    public String getDescription() {
        return String.format("UDP Receiver port %d In %d bps Ok %d KO %d", port,
                fastStats.bytesIn, fastStats.sentences, fastStats.sentenceErrs);
    }

    private void loop() {
        while (!stop) {
            try (DatagramSocket socket = new DatagramSocket(port)) {
                socket.setSoTimeout(SOCKET_READ_TIMEOUT);
                getLogger().info("Opened Datagram socket {" + port + "}");
                while (!stop) {
                    loopRead(socket);
                }
            } catch (SocketException e) {
                getLogger().error("Erroro opening datagram socket", e);
            }
            if (!stop) Utils.pause(OPEN_SOCKET_RETRY_TIME);
        }
    }

    private void loopRead(DatagramSocket socket) {
        try {
            DatagramPacket p = new DatagramPacket(buffer, buffer.length);
            socket.receive(p);
            String sSentence = new String(p.getData(), 0, p.getLength());
            updateReadStats(sSentence);
            if (sSentence.startsWith("{\"timestamp\":\"")) {
                loopReadN2k(sSentence);
            } else if (sSentence.startsWith("$") || sSentence.startsWith("!")) {
                loopReadN0183(sSentence);
            }
        } catch (SocketTimeoutException e) {
            // read timeout
            getLogger().debug("Datagram socket read timeout");
            Utils.pause(1000);
        } catch (Exception e) {
            getLogger().warning("Error receiving sentence {" + e.getMessage() + "}");
        }
        updateReadSentencesStats(true);
    }

    private void loopReadN0183(String sSentence) {
        try {
            Sentence s = SentenceFactory.getInstance().createParser(sSentence);
            updateReadSentencesStats(false);
            onSentenceRead(s);
        } catch (Exception e) {
            getLogger().debug("Can't read sentence {" + sSentence + "} {" + e + "}");
        }
    }

    private void loopReadN2k(String sSentence) {
        try {
            CANBOATStream.PGNMessage m = n2kStream.getMessage(sSentence);
            if (m!=null) {
                Sentence s = decoder.getSentence(m.getPgn(), m.getFields());
                if (s != null) {
                    updateReadSentencesStats(false);
                    onSentenceRead(s);
                }
            }
        } catch (Exception e) {
            getLogger().debug("Can't read N2K sentence {" + sSentence + "} {" + e + "}");
        }
    }

    @Override
    protected boolean onActivate() {
	    synchronized (this) {
	        if (!isStarted()) {
                Thread t = new Thread(this::loop);
                t.start();
                return true;
            }
	    }
        return false;
	}
	
	@Override
	protected void onDeactivate() {
	    synchronized (this) {
	    	stop = true;
	    }
	}

	private void onSentenceRead(Sentence e) {
		notify(e);
	}
	
    @Override
    public String toString() {
        return " {UDP " + port + " R}";
    }

    private void updateReadStats(String s) {
        synchronized (stats) {
            int l = s.length() + 2;
            fastStats.bytesIn += l;
            stats.bytesIn += l;
        }
    }

    private void updateReadSentencesStats(boolean fail) {
        if (fail) {
            fastStats.sentenceErrs++;
            stats.sentenceErrs++;
        } else {
            fastStats.sentences++;
            stats.sentences++;
        }
    }

    @Override
    public void onTimer() {
        long t = getCache().getNow();
        synchronized (stats) {
            if (fastStats.resetTime == 0) {
                fastStats.reset(t);
                stats.reset(t);
            } else {
                if ((t - fastStats.resetTime) > FAST_STATS_PERIOD) {
                    fastStats.reset(t);
                }
                if ((t - stats.resetTime) > STATS_PERIOD) {
                    getLogger().info(String.format("BIn {%d} bpsIn {%d} Msg {%d} Err {%d}",
                            stats.bytesIn, (stats.bytesIn * 8 * 1000) / (t - stats.resetTime),
                            stats.sentences, stats.sentenceErrs));
                    stats.reset(t);
                }
            }
        }
        super.onTimer();
    }
}