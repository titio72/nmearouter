package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class N2KGenericMessageImpl implements N2KMessage {

    private static final DateTimeFormatter timeF = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss.SSS");

    private final N2KMessageHeader header;
    private final byte[] data;

    public N2KGenericMessageImpl(N2KMessageHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    @Override
    public N2KMessageHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(String.format("%s,%d,%d,%d,%d,%d",
                LocalDateTime.now().format(timeF), getHeader().getPriority(),
                getHeader().getPgn(), getHeader().getSource(), getHeader().getDest(),
                getData().length));
        for (byte datum : data) res.append(String.format(",%02x", datum));
        return res.toString();
    }
}
