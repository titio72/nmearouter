package com.aboni.nmea.router.n2k.can;

import com.google.common.hash.HashCode;

import javax.validation.constraints.NotNull;
import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public class CANDataFrame {

    private final long id;
    private final byte[] data;

    public static CANDataFrame create(long id, @NotNull byte[] data) {
        return new CANDataFrame(id, data);
    }

    private CANDataFrame(long id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public int getDataSize() {
        return data.length;
    }

    @Override
    public int hashCode() {
        return HashCode.fromLong(id).asInt() + HashCode.fromBytes(data).asInt();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CANDataFrame) {
            return id == ((CANDataFrame) o).id &&
                    Arrays.equals(data, ((CANDataFrame) o).data);
        } else return false;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("frame ").append(getId()).append(" [").append(getDataSize()).append("]");
        for (byte bt : data) b.append(String.format(" %02x", bt));
        return b.toString();

    }
}
