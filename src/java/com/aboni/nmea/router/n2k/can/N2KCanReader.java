package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.N2KMessageCallback;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefaultImpl;

import javax.inject.Inject;

public class N2KCanReader {

    private final TimestampProvider ts;

    private N2KMessageCallback callback;
    private N2KCanBusErrorCallback errCallback;

    public class Stats {
        private long frames;
        private long invalidFrames;
        private long lastReset;

        public long getFrames() {
            synchronized (this) {
                return frames;
            }
        }

        public long getInvalidFrames() {
            synchronized (this) {
                return invalidFrames;
            }
        }

        public void reset() {
            synchronized (this) {
                frames = 0;
                invalidFrames = 0;
                lastReset = ts.getNow();
            }
        }

        private void incrFrames(int n) {
            synchronized (this) {
                if (frames<Long.MAX_VALUE) frames++;
            }
        }

        private void incrInvalidFrames(int n) {
            synchronized (this) {
                if (invalidFrames<Long.MAX_VALUE) invalidFrames++;
            }
        }

        public long getLastResetTime() {
            synchronized (this) {
                return lastReset;
            }
        }

        public String toString(long t) {
            synchronized (this) {
                return String.format("Frames {%d} Invalid Frames {%d} Period {%d}", frames, invalidFrames, t - lastReset);
            }
        }

    }

    private final Stats stats = new Stats();

    @Inject
    public N2KCanReader(TimestampProvider ts) {
        this.ts = ts;
    }

    public void setCallback(N2KMessageCallback cback) {
        this.callback = cback;
    }

    public void setErrCallback(N2KCanBusErrorCallback errCallback) {
        this.errCallback = errCallback;
    }

    public boolean onRead(int[] b, int offset) {
        if (offset>2 && b[offset] == 0xaa && b[offset - 1] == 0xaa && b[offset - 2] == 0x55) {
            handleFrame(b, offset);
            return true;
        }
        return false;
    }

    public Stats getStats() {
        return stats;
    }

    private void handleFrame(int[] b, int offset) {
        int dataSize = (b[0] & 0x0F);
        long id;
        boolean ext = (b[0] & 0x20) != 0;
        if (offset == (2 + dataSize + (ext ? 4 : 2))) {
            stats.incrFrames(1);
            if (ext) {
                id = b[1] + (b[2] << 8) + (b[3] << 16) + ((long) b[4] << 24);
            } else {
                id = b[1] + (b[2] << 8);
            }
            dumpAnalyzerFormat(offset, b, dataSize, id);
        } else if (errCallback != null) {
            stats.incrInvalidFrames(1);
            byte[] errB = new byte[offset];
            for (int i = 0; i < offset; i++) {
                errB[i] = (byte) (b[i] & 0xFF);
            }
            errCallback.onError(errB);
        }
    }

    private void dumpAnalyzerFormat(int offset, int[] b, int dataSize, long id) {
        if (callback != null) {
            N2KHeader iso = new N2KHeader(id);
            byte[] data = new byte[dataSize];
            for (int i = 0; i < dataSize; i++) data[i] = (byte) (b[offset - 1 - dataSize + i] & 0xFF);
            N2KMessageDefaultImpl msg = new N2KMessageDefaultImpl(iso, data);
            callback.onMessage(msg);
        }
    }

    public static String dumpCanDumpFormat(int[] b, int dataSize, long id) {
        StringBuilder res = new StringBuilder(String.format("  %08x  [%d]", id, dataSize));
        for (int i = 0; i < dataSize; i++)
            res.append(String.format(" %02x", b[i]));

        return res.toString();
    }
}
