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
          "Name": "Repeat Indicator",
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
          "Id": "imoNumber",
          "Name": "IMO number",
          "BitLength": 32,
          "BitOffset": 40,
          "BitStart": 0,
          "Type": "Integer",
          "Resolution": 1,
          "Signed": false
        },
        {
          "Order": 5,
          "Id": "callsign",
          "Name": "Callsign",
          "BitLength": 56,
          "BitOffset": 72,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 6,
          "Id": "name",
          "Name": "Name",
          "BitLength": 160,
          "BitOffset": 128,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 7,
          "Id": "typeOfShip",
          "Name": "Type of ship",
          "BitLength": 8,
          "BitOffset": 288,
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
          "Order": 8,
          "Id": "length",
          "Name": "Length",
          "BitLength": 16,
          "BitOffset": 296,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 9,
          "Id": "beam",
          "Name": "Beam",
          "BitLength": 16,
          "BitOffset": 312,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 10,
          "Id": "positionReferenceFromStarboard",
          "Name": "Position reference from Starboard",
          "BitLength": 16,
          "BitOffset": 328,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 11,
          "Id": "positionReferenceFromBow",
          "Name": "Position reference from Bow",
          "BitLength": 16,
          "BitOffset": 344,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.1",
          "Signed": false
        },
        {
          "Order": 12,
          "Id": "etaDate",
          "Name": "ETA Date",
          "Description": "Days since January 1, 1970",
          "BitLength": 16,
          "BitOffset": 360,
          "BitStart": 0,
          "Units": "days",
          "Type": "Date",
          "Resolution": 1,
          "Signed": false
        },
        {
          "Order": 13,
          "Id": "etaTime",
          "Name": "ETA Time",
          "Description": "Seconds since midnight",
          "BitLength": 32,
          "BitOffset": 376,
          "BitStart": 0,
          "Units": "s",
          "Type": "Time",
          "Resolution": "0.0001",
          "Signed": false
        },
        {
          "Order": 14,
          "Id": "draft",
          "Name": "Draft",
          "BitLength": 16,
          "BitOffset": 408,
          "BitStart": 0,
          "Units": "m",
          "Resolution": "0.01",
          "Signed": false
        },
        {
          "Order": 15,
          "Id": "destination",
          "Name": "Destination",
          "BitLength": 160,
          "BitOffset": 424,
          "BitStart": 0,
          "Type": "ASCII text",
          "Signed": false
        },
        {
          "Order": 16,
          "Id": "aisVersionIndicator",
          "Name": "AIS version indicator",
          "BitLength": 2,
          "BitOffset": 584,
          "BitStart": 0,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "ITU-R M.1371-1",
              "value": "0"
            },
            {
              "name": "ITU-R M.1371-3",
              "value": "1"
            }
          ]
        },
        {
          "Order": 17,
          "Id": "gnssType",
          "Name": "GNSS type",
          "BitLength": 4,
          "BitOffset": 586,
          "BitStart": 2,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "undefined",
              "value": "0"
            },
            {
              "name": "GPS",
              "value": "1"
            },
            {
              "name": "GLONASS",
              "value": "2"
            },
            {
              "name": "GPS+GLONASS",
              "value": "3"
            },
            {
              "name": "Loran-C",
              "value": "4"
            },
            {
              "name": "Chayka",
              "value": "5"
            },
            {
              "name": "integrated",
              "value": "6"
            },
            {
              "name": "surveyed",
              "value": "7"
            },
            {
              "name": "Galileo",
              "value": "8"
            }
          ]
        },
        {
          "Order": 18,
          "Id": "dte",
          "Name": "DTE",
          "BitLength": 1,
          "BitOffset": 590,
          "BitStart": 6,
          "Type": "Lookup table",
          "Signed": false,
          "EnumValues": [
            {
              "name": "available",
              "value": "0"
            },
            {
              "name": "not available",
              "value": "1"
            }
          ]
        },
        {
          "Order": 19,
          "Id": "reserved",
          "Name": "Reserved",
          "Description": "reserved",
          "BitLength": 1,
          "BitOffset": 591,
          "BitStart": 7,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 20,
          "Id": "aisTransceiverInformation",
          "Name": "AIS Transceiver information",
          "BitLength": 5,
          "BitOffset": 592,
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
        }

     */
}