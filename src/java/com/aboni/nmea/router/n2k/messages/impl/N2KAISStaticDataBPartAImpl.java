package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.AIS_TRANSCEIVER;
import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.REPEAT_INDICATOR;
import static com.aboni.nmea.router.n2k.messages.N2kMessagePGNs.AIS_STATIC_DATA_CLASS_B_PART_A_PGN;

public class N2KAISStaticDataBPartAImpl extends N2KMessageImpl implements AISStaticData {

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private String name;
    private String transceiverInfo;
    private int seqId;

    public N2KAISStaticDataBPartAImpl(byte[] data) {
        super(getDefaultHeader(AIS_STATIC_DATA_CLASS_B_PART_A_PGN), data);
        fill();
    }

    public N2KAISStaticDataBPartAImpl(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != AIS_STATIC_DATA_CLASS_B_PART_A_PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", AIS_STATIC_DATA_CLASS_B_PART_A_PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        messageId = (int) parseIntegerSafe(data, 0, 0, 6, 0xFF);
        repeatIndicator = parseEnum(data, 6, 6, 2, N2KLookupTables.getTable(REPEAT_INDICATOR));
        sMMSI = String.format("%d", parseIntegerSafe(data, 8, 0, 32, 0));
        name = getText(data, 5, 20);
        transceiverInfo = parseEnum(data, 200, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));
        seqId = (int) parseIntegerSafe(data, 208, 0, 8, 0xFF);
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
        return name;
    }

    @Override
    public String getCallSign() {
        return null;
    }

    public int getSeqId() {
        return seqId;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public String getTypeOfShip() {
        return null;
    }

    @Override
    public String getAisTransceiverInfo() {
        return transceiverInfo;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public double getBeam() {
        return 0;
    }

    @Override
    public String toString() {
        return String.format("PGN {%s} Source {%d} MsgId {%d} Repeat {%s} Name {%s} MMSI {%s} AISClass {%s} Transceiver {%s}",
                AIS_STATIC_DATA_CLASS_B_PART_A_PGN, getHeader().getSource(),
                getMessageId(), getRepeatIndicator(), getName(), getMMSI(),
                getAISClass(), getAisTransceiverInfo());
    }
}