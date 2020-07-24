package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAISStaticDataA extends N2KMessageImpl implements AISStaticData {

    public static final int PGN = 129794;

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

    public N2KAISStaticDataA(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISStaticDataA(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, false, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.LOOKUP_REPEAT_INDICATOR);
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, false, 0));
        imo = (int) parseIntegerSafe(data, 40, 0, 32, false, 0xFFFFFF);
        callSign = parseAscii(data, 72, 0, 56);
        name = parseAscii(data, 128, 0, 160);
        typeOfShip = parseEnum(data, 288, 0, 8, N2KLookupTables.LOOKUP_SHIP_TYPE);
        length = parseDoubleSafe(data, 296, 0, 16, 0.1, false);
        beam = parseDoubleSafe(data, 312, 0, 16, 0.1, false);
        aisTransceiverInformation = parseEnum(data, 592, 0, 5, N2KLookupTables.LOOKUP_AIS_TRANSCEIVER);

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
}