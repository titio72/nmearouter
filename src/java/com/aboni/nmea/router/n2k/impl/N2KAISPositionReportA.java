package com.aboni.nmea.router.n2k.impl;


import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

public class N2KAISPositionReportA extends N2KMessageImpl implements AISPositionReport {

    public static final int PGN = 129038;

    public N2KAISPositionReportA(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISPositionReportA(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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

        double lon = parseDoubleSafe(data, 40, 0, 32, 0.0000001, true);
        double lat = parseDoubleSafe(data, 72, 0, 32, 0.0000001, true);
        if (!(Double.isNaN(lon) || Double.isNaN(lat))) {
            position = new Position(lat, lon);
        }

        positionAccuracy = parseEnum(data, 104, 0, 1, N2KLookupTables.LOOKUP_POSITION_ACCURACY);
        sRAIM = parseIntegerSafe(data, 105, 1, 1, false, 0) == 1;
        timestamp = (int) parseIntegerSafe(data, 106, 2, 6, false, 0xFF);

        cog = parseDoubleSafe(data, 112, 0, 16, 0.0001, false);
        cog = Double.isNaN(cog) ? cog : Utils.round(Math.toDegrees(cog), 1);
        sog = parseDoubleSafe(data, 128, 0, 16, 0.01, false);
        heading = parseDoubleSafe(data, 168, 0, 16, 0.0001, false);
        heading = Double.isNaN(heading) ? heading : Utils.round(Math.toDegrees(heading), 1);
        rateOfTurn = parseDoubleSafe(data, 184, 0, 16, 0.0001, false);
        rateOfTurn = Double.isNaN(rateOfTurn) ? heading : Utils.round(Math.toDegrees(rateOfTurn), 1);

        aisSpare = (int) parseIntegerSafe(data, 208, 0, 3, false, 0xFF);
        seqId = (int) parseIntegerSafe(data, 216, 0, 8, false, 0xFF);

        aisTransceiverInfo = parseEnum(data, 163, 3, 5, N2KLookupTables.LOOKUP_AIS_TRANSCEIVER);

        navStatus = parseEnum(data, 200, 0, 4, N2KLookupTables.LOOKUP_NAV_STATUS);

        specialManeuverIndicator = parseEnum(data, 204, 4, 2, N2KLookupTables.LOOKUP_AIS_SPECIAL_MANEUVER);
    }

    private int messageId;
    private String repeatIndicator;
    private String sMMSI;
    private Position position;
    private String positionAccuracy;
    private boolean sRAIM;
    private int timestamp;
    private double cog;
    private double sog;
    private double heading;
    private double rateOfTurn;
    private String aisTransceiverInfo;
    private String navStatus;
    private String specialManeuverIndicator;
    private int aisSpare;
    private int seqId;

    @Override
    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getAISClass() {
        return "A";
    }

    public Position getPosition() {
        return position;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    public boolean issRAIM() {
        return sRAIM;
    }

    public double getCog() {
        return cog;
    }

    public double getSog() {
        return sog;
    }

    public double getHeading() {
        return heading;
    }

    public double getRateOfTurn() {
        return rateOfTurn;
    }

    public String getMMSI() {
        return sMMSI;
    }

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

    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public int getAisSpare() {
        return aisSpare;
    }

    public int getSeqId() {
        return seqId;
    }

    @Override
    public String getNavStatus() {
        return navStatus;
    }

    public String getSpecialManeuverIndicator() {
        return specialManeuverIndicator;
    }

    /*
        {
          "Order": 1, "Id": "messageId", "Name": "Message ID", "BitLength": 6, "BitOffset": 0, "BitStart": 0, "Signed": false
          "Order": 2, "Id": "repeatIndicator", "Name": "Repeat Indicator", "BitLength": 2, "BitOffset": 6, "BitStart": 6, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Initial", "value": "0"
              "name": "First retransmission", "value": "1"
              "name": "Second retransmission", "value": "2"
              "name": "Final retransmission", "value": "3"
            }
          ]
          "Order": 3, "Id": "userId", "Name": "User ID", "BitLength": 32, "BitOffset": 8, "BitStart": 0, "Units": "MMSI", "Type": "Integer", "Resolution": 1, "Signed": false
          "Order": 4, "Id": "longitude", "Name": "Longitude", "BitLength": 32, "BitOffset": 40, "BitStart": 0, "Units": "deg", "Type": "Latitude", "Resolution": "0.0000001", "Signed": true
          "Order": 5, "Id": "latitude", "Name": "Latitude", "BitLength": 32, "BitOffset": 72, "BitStart": 0, "Units": "deg", "Type": "Longitude", "Resolution": "0.0000001", "Signed": true
          "Order": 6, "Id": "positionAccuracy", "Name": "Position Accuracy", "BitLength": 1, "BitOffset": 104, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Low", "value": "0"
              "name": "High", "value": "1"
            }
          ]
          "Order": 7, "Id": "raim", "Name": "RAIM", "BitLength": 1, "BitOffset": 105, "BitStart": 1, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "not in use", "value": "0"
              "name": "in use", "value": "1"
            }
          ]
          "Order": 8, "Id": "timeStamp", "Name": "Time Stamp", "Description": "0-59 = UTC second when the report was generated", "BitLength": 6, "BitOffset": 106, "BitStart": 2, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Not available", "value": "60"
              "name": "Manual input mode", "value": "61"
              "name": "Dead reckoning mode", "value": "62"
              "name": "Positioning system is inoperative", "value": "63"
            }
          ]
          "Order": 9, "Id": "cog", "Name": "COG", "BitLength": 16, "BitOffset": 112, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
          "Order": 10, "Id": "sog", "Name": "SOG", "BitLength": 16, "BitOffset": 128, "BitStart": 0, "Units": "m/s", "Resolution": "0.01", "Signed": false
          "Order": 11, "Id": "communicationState", "Name": "Communication State", "Description": "Information used by the TDMA slot allocation algorithm and synchronization information", "BitLength": 19, "BitOffset": 144, "BitStart": 0, "Type": "Binary data", "Signed": false
          "Order": 12, "Id": "aisTransceiverInformation", "Name": "AIS Transceiver information", "BitLength": 5, "BitOffset": 163, "BitStart": 3, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Channel A VDL reception", "value": "0"
              "name": "Channel B VDL reception", "value": "1"
              "name": "Channel A VDL transmission", "value": "2"
              "name": "Channel B VDL transmission", "value": "3"
              "name": "Own information not broadcast", "value": "4"
              "name": "Reserved", "value": "5"
            }
          ]
          "Order": 13, "Id": "heading", "Name": "Heading", "Description": "True heading", "BitLength": 16, "BitOffset": 168, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
          "Order": 14, "Id": "rateOfTurn", "Name": "Rate of Turn", "BitLength": 16, "BitOffset": 184, "BitStart": 0, "Units": "rad/s", "Resolution": 3.125e-05, "Signed": true
          "Order": 15, "Id": "navStatus", "Name": "Nav Status", "BitLength": 4, "BitOffset": 200, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Under way using engine", "value": "0"
              "name": "At anchor", "value": "1"
              "name": "Not under command", "value": "2"
              "name": "Restricted manoeuverability", "value": "3"
              "name": "Constrained by her draught", "value": "4"
              "name": "Moored", "value": "5"
              "name": "Aground", "value": "6"
              "name": "Engaged in Fishing", "value": "7"
              "name": "Under way sailing", "value": "8"
              "name": "Hazardous material - High Speed", "value": "9"
              "name": "Hazardous material - Wing in Ground", "value": "10"
              "name": "AIS-SART", "value": "14"
            }
          ]
          "Order": 16, "Id": "specialManeuverIndicator", "Name": "Special Maneuver Indicator", "BitLength": 2, "BitOffset": 204, "BitStart": 4, "Type": "Lookup table", "Signed": false, "EnumValues": [
            {
              "name": "Not available", "value": "0"
              "name": "Not engaged in special maneuver", "value": "1"
              "name": "Engaged in special maneuver", "value": "2"
              "name": "Reserverd", "value": "3"
            }
          ]
          "Order": 18, "Id": "aisSpare", "Name": "AIS Spare", "BitLength": 3, "BitOffset": 208, "BitStart": 0, "Type": "Binary data", "Signed": false
          "Order": 20, "Id": "sequenceId", "Name": "Sequence ID", "BitLength": 8, "BitOffset": 216, "BitStart": 0, "Type": "Integer", "Resolution": 1, "Signed": false
        }


     */
}