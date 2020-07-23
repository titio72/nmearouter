package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public class N2KAISStaticDataB_PartB extends N2KMessageImpl implements AISStaticData {

    public static final int PGN = 129810;

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private String typeOfShip;
    private String callSign;
    private double length;
    private double beam;
    private String aisTransceiverInformation;

    public N2KAISStaticDataB_PartB(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISStaticDataB_PartB(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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
        typeOfShip = parseEnum(data, 40, 0, 8, N2KLookupTables.LOOKUP_SHIP_TYPE);
        callSign = parseAscii(data, 104, 0, 56);
        length = parseDoubleSafe(data, 160, 0, 16, 0.1, false);
        beam = parseDoubleSafe(data, 176, 0, 16, 0.1, false);
        aisTransceiverInformation = parseEnum(data, 264, 0, 5, N2KLookupTables.LOOKUP_AIS_TRANSCEIVER);
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
          "Id": "typeOfShip",
          "Name": "Type of ship",
          "BitLength": 8,
          "BitOffset": 40,
          "BitStart": 0,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "unavailable",
              "value": "0"
            },
            {
              "name": "Wing In Ground",
              "value": "20"
            },
            {
              "name": "Wing In Ground (no other information)",
              "value": "29"
            },
            {
              "name": "Fishing",
              "value": "30"
            },
            {
              "name": "Towing",
              "value": "31"
            },
            {
              "name": "Towing exceeds 200m or wider than 25m",
              "value": "32"
            },
            {
              "name": "Engaged in dredging or underwater operations",
              "value": "33"
            },
            {
              "name": "Engaged in diving operations",
              "value": "34"
            },
            {
              "name": "Engaged in military operations",
              "value": "35"
            },
            {
              "name": "Sailing",
              "value": "36"
            },
            {
              "name": "Pleasure",
              "value": "37"
            },
            {
              "name": "High speed craft",
              "value": "40"
            },
            {
              "name": "High speed craft carrying dangerous goods",
              "value": "41"
            },
            {
              "name": "High speed craft hazard cat B",
              "value": "42"
            },
            {
              "name": "High speed craft hazard cat C",
              "value": "43"
            },
            {
              "name": "High speed craft hazard cat D",
              "value": "44"
            },
            {
              "name": "High speed craft (no additional information)",
              "value": "49"
            },
            {
              "name": "Pilot vessel",
              "value": "50"
            },
            {
              "name": "SAR",
              "value": "51"
            },
            {
              "name": "Tug",
              "value": "52"
            },
            {
              "name": "Port tender",
              "value": "53"
            },
            {
              "name": "Anti-pollution",
              "value": "54"
            },
            {
              "name": "Law enforcement",
              "value": "55"
            },
            {
              "name": "Spare",
              "value": "56"
            },
            {
              "name": "Spare #2",
              "value": "57"
            },
            {
              "name": "Medical",
              "value": "58"
            },
            {
              "name": "RR Resolution No.18",
              "value": "59"
            },
            {
              "name": "Passenger ship",
              "value": "60"
            },
            {
              "name": "Passenger ship (no additional information)",
              "value": "69"
            },
            {
              "name": "Cargo ship",
              "value": "70"
            },
            {
              "name": "Cargo ship carrying dangerous goods",
              "value": "71"
            },
            {
              "name": "Cargo ship hazard cat B",
              "value": "72"
            },
            {
              "name": "Cargo ship hazard cat C",
              "value": "73"
            },
            {
              "name": "Cargo ship hazard cat D",
              "value": "74"
            },
            {
              "name": "Cargo ship (no additional information)",
              "value": "79"
            },
            {
              "name": "Tanker",
              "value": "80"
            },
            {
              "name": "Tanker carrying dangerous goods",
              "value": "81"
            },
            {
              "name": "Tanker hazard cat B",
              "value": "82"
            },
            {
              "name": "Tanker hazard cat C",
              "value": "83"
            },
            {
              "name": "Tanker hazard cat D",
              "value": "84"
            },
            {
              "name": "Tanker (no additional information)",
              "value": "89"
            },
            {
              "name": "Other",
              "value": "90"
            },
            {
              "name": "Other carrying dangerous goods",
              "value": "91"
            },
            {
              "name": "Other hazard cat B",
              "value": "92"
            },
            {
              "name": "Other hazard cat C",
              "value": "93"
            },
            {
              "name": "Other hazard cat D",
              "value": "94"
            },
            {
              "name": "Other (no additional information)",
              "value": "99"
            }
          ]
        },
        {
          "Order": 5,
          "Id": "vendorId",
          "Name": "Vendor ID",
          "BitLength": 56,
          "BitOffset": 48,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 6,
          "Id": "callsign",
          "Name": "Callsign",
          "BitLength": 56,
          "BitOffset": 104,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 7,
          "Id": "length",
          "Name": "Length",
          "BitLength": 16,
          "BitOffset": 160,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 8,
          "Id": "beam",
          "Name": "Beam",
          "BitLength": 16,
          "BitOffset": 176,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 9,
          "Id": "positionReferenceFromStarboard",
          "Name": "Position reference from Starboard",
          "BitLength": 16,
          "BitOffset": 192,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 10,
          "Id": "positionReferenceFromBow",
          "Name": "Position reference from Bow",
          "BitLength": 16,
          "BitOffset": 208,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 11,
          "Id": "mothershipUserId",
          "Name": "Mothership User ID",
          "Description": "MMSI of mother ship sent by daughter vessels",
          "BitLength": 32,
          "BitOffset": 224,
          "BitStart": 0,
          "Units": "MMSI",
          "Type": "Integer",
          "Resolution": 1,
          "Signed": false
        },
        {
          "Order": 12,
          "Id": "reserved",
          "Name": "Reserved",
          "Description": "reserved",
          "BitLength": 2,
          "BitOffset": 256,
          "BitStart": 0,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 13,
          "Id": "spare",
          "Name": "Spare",
          "BitLength": 6,
          "BitOffset": 258,
          "BitStart": 2,
          "Type": "Integer",
          "Resolution": 1,
          "Signed": false
        },
        {
          "Order": 14,
          "Id": "aisTransceiverInformation",
          "Name": "AIS Transceiver information",
          "BitLength": 5,
          "BitOffset": 264,
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
          "Order": 15,
          "Id": "reserved",
          "Name": "Reserved",
          "Description": "reserved",
          "BitLength": 3,
          "BitOffset": 269,
          "BitStart": 5,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 16,
          "Id": "sequenceId",
          "Name": "Sequence ID",
          "BitLength": 8,
          "BitOffset": 272,
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
}