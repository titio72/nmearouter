package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.AISPositionReport;
import com.aboni.nmea.router.AISStaticData;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

import java.time.Instant;

import static com.aboni.nmea.router.n2k.N2KLookupTables.LOOKUP_MAPS.*;

public class N2KAISAtoN extends N2KMessageImpl implements AISPositionReport, AISStaticData {

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
    private String aisTransceiverInfo;
    private int aisSpare;
    private String positionFixingDeviceType;
    private long overrideTime = -1;

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

        sAtoNType = parseEnum(data, 176, 0, 5, N2KLookupTables.getTable(ATON_TYPE));

        name = getText(data, 26, 34);
        aisTransceiverInfo = parseEnum(data, 200, 0, 5, N2KLookupTables.getTable(AIS_TRANSCEIVER));
        aisSpare = (int) parseIntegerSafe(data, 184, 0, 1, 0xFF);
        positionFixingDeviceType = parseEnum(data, 185, 1, 4, N2KLookupTables.getTable(POSITION_FIX_DEVICE));
    }

    @Override
    public String getAISClass() {
        return "X";
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
    public String getTypeOfShip() {
        return "AtoN";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCallSign() {
        return "";
    }

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getNavStatus() {
        return "";
    }

    @Override
    public long getAge(long now) {
        if (getTimestamp() <= 60) {
            Instant l = (getOverrrideTime() > 0) ?
                    Instant.ofEpochMilli(getOverrrideTime()) :
                    getHeader().getTimestamp().plusNanos((long) (getTimestamp() * 1E06));
            return now - l.toEpochMilli();
        } else {
            return -1;
        }
    }

    @Override
    public String getTimestampStatus() {
        switch (timestamp) {
            case 0xFF:
            case 0x60:
                return "Not available";
            case 61:
                return "Manual input mode";
            case 62:
                return "Dead reckoning mode";
            case 63:
                return "Positioning system is inoperative";
            default:
                return "Available";
        }
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
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean issRAIM() {
        return sRAIM;
    }

    @Override
    public double getCog() {
        return 0;
    }

    @Override
    public double getSog() {
        return 0;
    }

    @Override
    public double getHeading() {
        return 0;
    }

    @Override
    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    public int getAisSpare() {
        return aisSpare;
    }

    @Override
    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public String getAtoNType() {
        return sAtoNType;
    }

    public String getPositionFixingDeviceType() {
        return positionFixingDeviceType;
    }

    @Override
    public void setOverrideTime(long t) {
        overrideTime = t;
    }

    @Override
    public long getOverrrideTime() {
        return overrideTime;
    }
    /*
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
*/
}
