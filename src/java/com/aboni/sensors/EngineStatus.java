package com.aboni.sensors;

public enum EngineStatus {
    UNKNOWN(2),
    ON(1),
    OFF(0);

    private final int value;

    EngineStatus(int i) {
        this.value = i;
    }

    public int getValue() {
        return value;
    }

    public static EngineStatus valueOf(int b) {
        switch (b) {
            case 0:
                return OFF;
            case 1:
                return ON;
            default:
                return UNKNOWN;
        }
    }

    public byte toByte() {
        return (byte) value;
    }

    @Override
    public String toString() {
        switch (value) {
            case 0: return "Off";
            case 1: return "On";
            default: return "Unknown";
        }
    }
}
