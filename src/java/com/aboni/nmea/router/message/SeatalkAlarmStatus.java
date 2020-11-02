package com.aboni.nmea.router.message;

public enum SeatalkAlarmStatus {

    OFF(0, "Alarm condition not met"),
    ON(1, "Alarm condition met and not silenced"),
    SILENCED(2, "Alarm condition met and silenced");

    private final int value;
    private final String description;

    SeatalkAlarmStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static SeatalkAlarmStatus valueOf(int v) {
        switch (v) {
            case 0: return OFF;
            case 1: return ON;
            case 2: return SILENCED;
            default: return null;
        }
    }

    @Override
    public String toString() {
        return description;
    }

    public int getValue() {
        return value;
    }
}
