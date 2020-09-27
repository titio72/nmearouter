package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.N2KMessageCallback;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageDefaultImpl;

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

        private void incrementFrames() {
            synchronized (this) {
                if (frames < Long.MAX_VALUE) frames += 1;
            }
        }

        private void incrementInvalidFrames() {
            synchronized (this) {
                if (invalidFrames < Long.MAX_VALUE) invalidFrames += 1;
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

    public void setCallback(N2KMessageCallback callback) {
        this.callback = callback;
    }

    public void setErrCallback(N2KCanBusErrorCallback errCallback) {
        this.errCallback = errCallback;
    }

    public boolean onRead(int[] b, int offset) {
        if (offset > 2 && b[offset] == 0xaa && b[offset - 1] == 0x55) {
            handleFrame(b, offset);
            return true;
        }
        return false;
    }

    public Stats getStats() {
        return stats;
    }

    private boolean isDataPackage(int type) {
        return ((type >> 6) ^ 3) == 0;
    }

    private static boolean isExt(int type) {
        return (type & 0x20) != 0;
    }

    private static long getExtId(int[] b) {
        return b[2] + (b[3] << 8) + (b[4] << 16) + ((long) b[5] << 24);
    }

    private static long getId(int[] b) {
        return b[2] + ((long) b[3] << 8);
    }

    private static boolean checkBufferSize(int l, int dataSize, boolean ext) {
        // 3 because we have the initial 0xaa, the final 0x55 and the type (first byte after the 0xaa)
        return l == (3 + dataSize + (ext ? 4 : 2));
    }

    private void handleFrame(int[] b, int offset) {
        if (isDataPackage(b[1])) {
            int dataSize = (b[1] & 0x0F);
            long id;
            boolean ext = isExt(b[1]);
            if (checkBufferSize(offset, dataSize, ext)) {
                stats.incrementFrames();
                id = ext ? getExtId(b) : getId(b);
                dumpAnalyzerFormat(offset, b, dataSize, id);
            } else if (errCallback != null) {
                stats.incrementInvalidFrames();
                byte[] errB = new byte[offset];
                for (int i = 0; i < offset; i++) {
                    errB[i] = (byte) (b[i] & 0xFF);
                }
                errCallback.onError(errB);
            }
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
