package com.aboni.nmea.router;

import java.time.Instant;
import java.time.ZoneOffset;

public abstract class TimestampProvider {

    private long skew;
    private boolean synced = false;

    /**
     * Get the current timestamp.
     *
     * @return the UNIX time of "now"
     */
    public abstract long getNow();

    /**
     * It indicates if the timestamp is reliable, typically synchronized with the GPS.
     * If the timestamps are not synced with GPS they can still be used for timeouts, but to timestamp events.
     *
     * @return true when the timestamp is reliable, false otherwise
     */
    public boolean isSynced() {
        return synced;
    }

    /**
     * Determines whether the reference time (usually the GPS time) is synced within a given tolerance.
     *
     * @param referenceTime The reference time (UNIX time).
     * @param tolerance     The tolerance in ms.
     * @return
     * @see TimestampProvider:isSynced()
     */
    public boolean setSkew(long referenceTime, long tolerance) {
        skew = getNow() - referenceTime;
        synced = Math.abs(skew) < tolerance;
        return isSynced();
    }

    /**
     * The skew from the reference time.
     *
     * @return the skew milliseconds.
     */
    public long getSkew() {
        return skew;
    }

    public Instant getInstant() {
        return Instant.ofEpochMilli(getNow());
    }

    public int getYear() {
        return getInstant().atOffset(ZoneOffset.UTC).getYear();
    }
}
