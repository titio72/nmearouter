package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAISStaticDataB_PartA extends N2KMessageImpl implements AISStaticData {

    public static final int PGN = 129809;

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private String name;
    private String transceiverInfo;
    private int seqId;

    public N2KAISStaticDataB_PartA(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISStaticDataB_PartA(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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
        name = parseAscii(data, 40, 0, 160);
        transceiverInfo = parseEnum(data, 200, 0, 5, N2KLookupTables.LOOKUP_AIS_TRANSCEIVER);
        seqId = (int) parseIntegerSafe(data, 208, 0, 8, false, 0xFF);

    /*
       {
          "Order": 1,
          "Id": "messageId",
          "Name": "Message ID",
          "BitLength": 6,
          "BitOffset": 0,
          "BitStart": 0,
          "Signed": false
        },
        {
          "Order": 2,
          "Id": "repeatIndicator",
          "Name": "Repeat indicator",
          "BitLength": 2,
          "BitOffset": 6,
          "BitStart": 6,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "Initial",
              "value": "0"
            },
            {
              "name": "First retransmission",
              "value": "1"
            },
            {
              "name": "Second retransmission",
              "value": "2"
            },
            {
              "name": "Final retransmission",
              "value": "3"
            }
          ]
        },
        {
          "Order": 3,
          "Id": "userId",
          "Name": "User ID",
          "BitLength": 32,
          "BitOffset": 8,
          "BitStart": 0,
          "Units": "MMSI",
          "Type": "Integer",
          "Resolution": 1,
          "Signed": false
        },
        {
          "Order": 4,
          "Id": "name",
          "Name": "Name",
          "BitLength": 160,
          "BitOffset": 40,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 5,
          "Id": "aisTransceiverInformation",
          "Name": "AIS Transceiver information",
          "BitLength": 5,
          "BitOffset": 200,
          "BitStart": 0,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "Channel A VDL reception",
              "value": "0"
            },
            {
              "name": "Channel B VDL reception",
              "value": "1"
            },
            {
              "name": "Channel A VDL transmission",
              "value": "2"
            },
            {
              "name": "Channel B VDL transmission",
              "value": "3"
            },
            {
              "name": "Own information not broadcast",
              "value": "4"
            },
            {
              "name": "Reserved",
              "value": "5"
            }
          ]
        },
        {
          "Order": 6,
          "Id": "reserved",
          "Name": "Reserved",
          "Description": "reserved",
          "BitLength": 3,
          "BitOffset": 205,
          "BitStart": 5,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 7,
          "Id": "sequenceId",
          "Name": "Sequence ID",
          "BitLength": 8,
          "BitOffset": 208,
          "BitStart": 0,
          "Type": "Integer",
          "Resolution": 1,
          "Signed": false
        }

     */

    }

    public static int getPGN() {
        return PGN;
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
}