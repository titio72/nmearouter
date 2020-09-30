package com.aboni.nmea.router.n2k.can;

public class CANReaderStats {

    private long dataFrames;
    private long invalidFrames;
    private long otherFrames;
    private long lastReset;

    public long getDataFrames() {
        synchronized (this) {
            return dataFrames;
        }
    }

    public long getOtherFrames() {
        synchronized (this) {
            return otherFrames;
        }
    }

    public long getInvalidFrames() {
        synchronized (this) {
            return invalidFrames;
        }
    }

    public void reset(long time) {
        synchronized (this) {
            dataFrames = 0;
            invalidFrames = 0;
            otherFrames = 0;
            lastReset = time;
        }
    }

    public void incrementDataFrames() {
        synchronized (this) {
            if (dataFrames < Long.MAX_VALUE) dataFrames += 1;
        }
    }

    public void incrementOtherFrames() {
        synchronized (this) {
            if (otherFrames < Long.MAX_VALUE) otherFrames += 1;
        }
    }

    public void incrementInvalidFrames() {
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
            return String.format("Data Frames {%d} Invalid Frames {%d} Other Frames {%d} Period {%d}",
                    dataFrames, invalidFrames, otherFrames, t - lastReset);
        }
    }

}
