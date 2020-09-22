package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KAISStaticDataBPartBImpl extends N2KMessageImpl implements AISStaticData {

    public static final int PGN = 129810;

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private String typeOfShip;
    private String callSign;
    private double length;
    private double beam;
    private String aisTransceiverInformation;

    public N2KAISStaticDataBPartBImpl(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISStaticDataBPartBImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, 0));
        typeOfShip = parseEnum(data, 40, 0, 8, N2KLookupTables.getTable(SHIP_TYPE));
        callSign = getText(data, 13, 7);
        length = parseDoubleSafe(data, 160, 16, 0.1, false);
        beam = parseDoubleSafe(data, 176, 16, 0.1, false);
        aisTransceiverInformation = parseEnum(data, 264, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
    }

    @Override
    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    @Override
    public String getName() {
        return null;
    }

    public String getCallSign() {
        return callSign;
    }

    public double getLength() {
        return length;
    }

    public double getBeam() {
        return beam;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public String getTypeOfShip() {
        return typeOfShip;
    }

    @Override
    public String getAisTransceiverInfo() {
        return aisTransceiverInformation;
    }


    @Override
    public String toString() {
        return String.format("PGN {%s} Src {%d} MsgId {%d} MMSI {%s} Repeat {%s} Type {%s} Callsign {%s} " +
                        "AISClass {%s} Length {%.1f} Beam {%.1f} Transceiver {%s}",
                PGN, getHeader().getSource(),
                getMessageId(), getMMSI(), getRepeatIndicator(), getTypeOfShip(), getCallSign(),
                getAISClass(), getLength(), getBeam(), getAisTransceiverInfo());
    }
}