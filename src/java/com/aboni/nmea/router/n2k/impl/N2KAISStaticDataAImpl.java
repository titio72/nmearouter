package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;
import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.AIS_STATIC_DATA_CLASS_A_PGN;

public class N2KAISStaticDataAImpl extends N2KMessageImpl implements AISStaticData {

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private int imo;
    private String callSign;
    private String name;
    private String typeOfShip;
    private double length;
    private double beam;
    private String aisTransceiverInformation;

    public N2KAISStaticDataAImpl(byte[] data) {
        super(getDefaultHeader(AIS_STATIC_DATA_CLASS_A_PGN), data);
        fill();
    }

    public N2KAISStaticDataAImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != AIS_STATIC_DATA_CLASS_A_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", AIS_STATIC_DATA_CLASS_A_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, 0));
        imo = (int) parseIntegerSafe(data, 40, 0, 32, 0xFFFFFF);
        callSign = getText(data, 9, 7);
        name = getText(data, 16, 20);
        typeOfShip = parseEnum(data, 288, 0, 8, N2KLookupTables.getTable(SHIP_TYPE));
        length = parseDoubleSafe(data, 296, 16, 0.1, false);
        beam = parseDoubleSafe(data, 312, 16, 0.1, false);
        aisTransceiverInformation = parseEnum(data, 592, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));

    }

    @Override
    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    public int getImo() {
        return imo;
    }

    public String getCallSign() {
        return callSign;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
    }

    @Override
    public String getTypeOfShip() {
        return typeOfShip;
    }

    @Override
    public String getAISClass() {
        return "A";
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public double getBeam() {
        return beam;
    }

    @Override
    public String getAisTransceiverInfo() {
        return aisTransceiverInformation;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} MsgId {%d} Repeat {%s} Name {%s} MMSI {%s} CallSign {%s} Type {%s} AISClass {%s} Transceiver {%s}",
                AIS_STATIC_DATA_CLASS_A_PGN, getHeader().getSource(),
                getMessageId(), getRepeatIndicator(), getName(), getMMSI(), getCallSign(),
                getTypeOfShip(), getAISClass(), getAisTransceiverInfo());
    }
}