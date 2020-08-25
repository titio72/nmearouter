package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.POSITION_ACCURACY;
import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.REPEAT_INDICATOR;

public class N2KAISAtoN extends N2KMessageImpl {

    public static final int PGN = 129041;

    private int messageId;
    private String sMMSI;
    private String repeatIndicator;
    private Position position;
    private String positionAccuracy;
    private boolean sRAIM;
    private int timestamp;
    private String name;
    private String sAtoNType;

    public N2KAISAtoN(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISAtoN(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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

        double lon = parseDoubleSafe(data, 40, 32, 0.0000001, true);
        double lat = parseDoubleSafe(data, 72, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            position = new Position(lat, lon);
        }

        positionAccuracy = parseEnum(data, 104, 0, 1, N2KLookupTables.getTable(POSITION_ACCURACY));
        sRAIM = parseIntegerSafe(data, 105, 1, 1, 0) == 1;
        timestamp = (int) parseIntegerSafe(data, 106, 2, 6, 0xFF);

        name = getText(data, 26, 34);

    }

    public int getMessageId() {
        return messageId;
    }

    public String getsMMSI() {
        return sMMSI;
    }

    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    public Position getPosition() {
        return position;
    }

    public boolean issRAIM() {
        return sRAIM;
    }

    public String getPositionAccuracy() {
        return positionAccuracy;
    }



    /*
    {
        "PGN": 129041,
            "Id": "aisAidsToNavigationAtonReport",
            "Description": "AIS Aids to Navigation (AtoN) Report",
            "Type": "Fast",
            "Complete": true,
            "Length": 60,
            "RepeatingFields": 0,
            "Fields": [
        {

        {
            "Order": 9,
                "Id": "lengthDiameter",
                "Name": "Length/Diameter",
                "BitLength": 16,
                "BitOffset": 112,
                "BitStart": 0,
                "Units": "m",
                "Resolution": "0.1",
                "Signed": false
        },
        {
            "Order": 10,
                "Id": "beamDiameter",
                "Name": "Beam/Diameter",
                "BitLength": 16,
                "BitOffset": 128,
                "BitStart": 0,
                "Units": "m",
                "Resolution": "0.1",
                "Signed": false
        },
        {
            "Order": 11,
                "Id": "positionReferenceFromStarboardEdge",
                "Name": "Position Reference from Starboard Edge",
                "BitLength": 16,
                "BitOffset": 144,
                "BitStart": 0,
                "Units": "m",
                "Resolution": "0.1",
                "Signed": false
        },
        {
            "Order": 12,
                "Id": "positionReferenceFromTrueNorthFacingEdge",
                "Name": "Position Reference from True North Facing Edge",
                "BitLength": 16,
                "BitOffset": 160,
                "BitStart": 0,
                "Units": "m",
                "Resolution": "0.1",
                "Signed": false
        },
        {
            "Order": 13,
                "Id": "atonType",
                "Name": "AtoN Type",
                "BitLength": 5,
                "BitOffset": 176,
                "BitStart": 0,
                "Type": "Lookup table",
                "Signed": false,
                "EnumValues": [
            {
                "name": "Default: Type of AtoN not specified",
                    "value": "0"
            },
            {
                "name": "Referece point",
                    "value": "1"
            },
            {
                "name": "RACON",
                    "value": "2"
            },
            {
                "name": "Fixed structure off-shore",
                    "value": "3"
            },
            {
                "name": "Reserved for future use",
                    "value": "4"
            },
            {
                "name": "Fixed light: without sectors",
                    "value": "5"
            },
            {
                "name": "Fixed light: with sectors",
                    "value": "6"
            },
            {
                "name": "Fixed leading light front",
                    "value": "7"
            },
            {
                "name": "Fixed leading light rear",
                    "value": "8"
            },
            {
                "name": "Fixed beacon: cardinal N",
                    "value": "9"
            },
            {
                "name": "Fixed beacon: cardinal E",
                    "value": "10"
            },
            {
                "name": "Fixed beacon: cardinal S",
                    "value": "11"
            },
            {
                "name": "Fixed beacon: cardinal W",
                    "value": "12"
            },
            {
                "name": "Fixed beacon: port hand",
                    "value": "13"
            },
            {
                "name": "Fixed beacon: starboard hand",
                    "value": "14"
            },
            {
                "name": "Fixed beacon: preferred channel port hand",
                    "value": "15"
            },
            {
                "name": "Fixed beacon: preferred channel starboard hand",
                    "value": "16"
            },
            {
                "name": "Fixed beacon: isolated danger",
                    "value": "17"
            },
            {
                "name": "Fixed beacon: safe water",
                    "value": "18"
            },
            {
                "name": "Fixed beacon: special mark",
                    "value": "19"
            },
            {
                "name": "Floating AtoN: cardinal N",
                    "value": "20"
            },
            {
                "name": "Floating AtoN: cardinal E",
                    "value": "21"
            },
            {
                "name": "Floating AtoN: cardinal S",
                    "value": "22"
            },
            {
                "name": "Floating AtoN: cardinal W",
                    "value": "23"
            },
            {
                "name": "Floating AtoN: port hand mark",
                    "value": "24"
            },
            {
                "name": "Floating AtoN: starboard hand mark",
                    "value": "25"
            },
            {
                "name": "Floating AtoN: preferred channel port hand",
                    "value": "26"
            },
            {
                "name": "Floating AtoN: preferred channel starboard hand",
                    "value": "27"
            },
            {
                "name": "Floating AtoN: isolated danger",
                    "value": "28"
            },
            {
                "name": "Floating AtoN: safe water",
                    "value": "29"
            },
            {
                "name": "Floating AtoN: special mark",
                    "value": "30"
            },
            {
                "name": "Floating AtoN: light vessel/LANBY/rigs",
                    "value": "31"
            }
          ]
        },
        {
            "Order": 14,
                "Id": "offPositionIndicator",
                "Name": "Off Position Indicator",
                "BitLength": 1,
                "BitOffset": 181,
                "BitStart": 5,
                "Type": "Lookup table",
                "Signed": false,
                "EnumValues": [
            {
                "name": "No",
                    "value": "0"
            },
            {
                "name": "Yes",
                    "value": "1"
            },
            {
                "name": "Error",
                    "value": "10"
            },
            {
                "name": "Unavailable",
                    "value": "11"
            }
          ]
        },
        {
            "Order": 15,
                "Id": "virtualAtonFlag",
                "Name": "Virtual AtoN Flag",
                "BitLength": 1,
                "BitOffset": 182,
                "BitStart": 6,
                "Type": "Lookup table",
                "Signed": false,
                "EnumValues": [
            {
                "name": "No",
                    "value": "0"
            },
            {
                "name": "Yes",
                    "value": "1"
            },
            {
                "name": "Error",
                    "value": "10"
            },
            {
                "name": "Unavailable",
                    "value": "11"
            }
          ]
        },
        {
            "Order": 16,
                "Id": "assignedModeFlag",
                "Name": "Assigned Mode Flag",
                "BitLength": 1,
                "BitOffset": 183,
                "BitStart": 7,
                "Type": "Lookup table",
                "Signed": false,
                "EnumValues": [
            {
                "name": "Autonomous and continuous",
                    "value": "0"
            },
            {
                "name": "Assigned mode",
                    "value": "1"
            }
          ]
        },
        {
            "Order": 17,
                "Id": "aisSpare",
                "Name": "AIS Spare",
                "BitLength": 1,
                "BitOffset": 184,
                "BitStart": 0,
                "Type": "Binary data",
                "Signed": false
        },
        {
            "Order": 18,
                "Id": "positionFixingDeviceType",
                "Name": "Position Fixing Device Type",
                "BitLength": 4,
                "BitOffset": 185,
                "BitStart": 1,
                "Type": "Lookup table",
                "Signed": false,
                "EnumValues": [
            {
                "name": "Default: undefined",
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
                "name": "Combined GPS/GLONASS",
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
                "name": "Integrated navigation system",
                    "value": "6"
            },
            {
                "name": "Surveyed",
                    "value": "7"
            },
            {
                "name": "Galileo",
                    "value": "8"
            },
            {
                "name": "Internal GNSS",
                    "value": "15"
            }
          ]
        },
        {
            "Order": 19,
                "Id": "reserved",
                "Name": "Reserved",
                "BitLength": 3,
                "BitOffset": 189,
                "BitStart": 5,
                "Type": "Binary data",
                "Signed": false
        },
        {
            "Order": 20,
                "Id": "atonStatus",
                "Name": "AtoN Status",
                "Description": "00000000 = default",
                "BitLength": 8,
                "BitOffset": 192,
                "BitStart": 0,
                "Type": "Binary data",
                "Signed": false
        },
        {
            "Order": 21,
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
            "Order": 22,
                "Id": "reserved",
                "Name": "Reserved",
                "BitLength": 3,
                "BitOffset": 205,
                "BitStart": 5,
                "Type": "Binary data",
                "Signed": false
        },
        {
            "Order": 23,
                "Id": "atonName",
                "Name": "AtoN Name",
                "BitLength": 272,
                "BitOffset": 208,
                "BitStart": 0,
                "Type": "ASCII or UNICODE string starting with length and control byte",
                "Signed": false
        }
      ]
    },*/
}
