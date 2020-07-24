package com.aboni.nmea.router.n2k.impl;


import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.N2KLookupTables;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import net.sf.marineapi.nmea.util.Position;

public class N2KAISPositionReportB extends N2KMessageImpl implements AISPositionReport {

    public static final int PGN = 129039;

    public N2KAISPositionReportB(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KAISPositionReportB(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
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

        aisTransceiverInfo = parseEnum(data, 163, 3, 5, N2KLookupTables.LOOKUP_AIS_TRANSCEIVER);

        int i = (int) parseIntegerSafe(data, 194, 2, 1, false, 0xFF);
        unitType = getUnitType(i);

        integratedDisplay = parseIntegerSafe(data, 195, 3, 1, false, 0) == 1;
        sDSC = parseIntegerSafe(data, 196, 4, 1, false, 0) == 1;
        canHandleMsg22 = parseIntegerSafe(data, 198, 6, 1, false, 0) == 1;

        band = parseIntegerSafe(data, 197, 5, 1, false, 0) == 0 ? "top 525 kHz of marine band" : "Entire marine band";
        aisMode = parseIntegerSafe(data, 199, 7, 1, false, 0) == 0 ? "Autonomous" : "Assigned";
        aisCommunicationState = parseIntegerSafe(data, 200, 0, 1, false, 0) == 0 ? "SOTDMA" : "ITDMA";
    }

    private String getUnitType(int i) {
        switch (i) {
            case 0:
                return "SOTDMA";
            case 1:
                return "CS";
            default:
                return null;
        }
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
    private String aisTransceiverInfo;
    private String unitType;
    private boolean integratedDisplay;
    private boolean sDSC;
    private String band;
    private boolean canHandleMsg22;
    private String aisMode;
    private String aisCommunicationState;

    public String getBand() {
        return band;
    }

    public String getAisMode() {
        return aisMode;
    }

    public boolean isIntegratedDisplay() {
        return integratedDisplay;
    }

    public boolean isCanHandleMsg22() {
        return canHandleMsg22;
    }

    public boolean isDSC() {
        return sDSC;
    }

    public String getUnitType() {
        return unitType;
    }

    public int getTimestamp() {
        return timestamp;
    }

    @Override
    public String getNavStatus() {
        return null;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public int getMessageId() {
        return messageId;
    }

    @Override
    public String getPositionAccuracy() {
        return positionAccuracy;
    }

    @Override
    public String getRepeatIndicator() {
        return repeatIndicator;
    }

    @Override
    public boolean issRAIM() {
        return sRAIM;
    }

    @Override
    public double getCog() {
        return cog;
    }

    @Override
    public double getSog() {
        return sog;
    }

    @Override
    public double getHeading() {
        return heading;
    }

    @Override
    public String getMMSI() {
        return sMMSI;
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

    public String getAisTransceiverInfo() {
        return aisTransceiverInfo;
    }

    public String getAisCommunicationState() {
        return aisCommunicationState;
    }

    /*
"Order": 1, "Id": "messageId", "Name": "Message ID", "BitLength": 6, "BitOffset": 0, "BitStart": 0, "Signed": false
"Order": 2, "Id": "repeatIndicator", "Name": "Repeat Indicator", "BitLength": 2, "BitOffset": 6, "BitStart": 6, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "Initial", "value": "0"
  "name": "First retransmission", "value": "1"
  "name": "Second retransmission", "value": "2"
  "name": "Final retransmission", "value": "3"
"Order": 3, "Id": "userId", "Name": "User ID", "BitLength": 32, "BitOffset": 8, "BitStart": 0, "Units": "MMSI", "Type": "Integer", "Resolution": 1, "Signed": false
"Order": 4, "Id": "longitude", "Name": "Longitude", "BitLength": 32, "BitOffset": 40, "BitStart": 0, "Units": "deg", "Type": "Longitude", "Resolution": "0.0000001", "Signed": true
"Order": 5, "Id": "latitude", "Name": "Latitude", "BitLength": 32, "BitOffset": 72, "BitStart": 0, "Units": "deg", "Type": "Latitude", "Resolution": "0.0000001", "Signed": true
"Order": 6, "Id": "positionAccuracy", "Name": "Position Accuracy", "BitLength": 1, "BitOffset": 104, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "Low", "value": "0"
  "name": "High", "value": "1"
"Order": 7, "Id": "raim", "Name": "RAIM", "BitLength": 1, "BitOffset": 105, "BitStart": 1, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "not in use", "value": "0"
  "name": "in use", "value": "1"
"Order": 8, "Id": "timeStamp", "Name": "Time Stamp", "Description": "0-59 = UTC second when the report was generated", "BitLength": 6, "BitOffset": 106, "BitStart": 2, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "Not available", "value": "60"
  "name": "Manual input mode", "value": "61"
  "name": "Dead reckoning mode", "value": "62"
  "name": "Positioning system is inoperative", "value": "63"
"Order": 9, "Id": "cog", "Name": "COG", "BitLength": 16, "BitOffset": 112, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
"Order": 10, "Id": "sog", "Name": "SOG", "BitLength": 16, "BitOffset": 128, "BitStart": 0, "Units": "m/s", "Resolution": "0.01", "Signed": false
"Order": 11, "Id": "communicationState", "Name": "Communication State", "Description": "Information used by the TDMA slot allocation algorithm and synchronization information", "BitLength": 19, "BitOffset": 144, "BitStart": 0, "Type": "Binary data", "Signed": false
"Order": 12, "Id": "aisTransceiverInformation", "Name": "AIS Transceiver information", "BitLength": 5, "BitOffset": 163, "BitStart": 3, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "Channel A VDL reception", "value": "0"
  "name": "Channel B VDL reception", "value": "1"
  "name": "Channel A VDL transmission", "value": "2"
  "name": "Channel B VDL transmission", "value": "3"
  "name": "Own information not broadcast", "value": "4"
  "name": "Reserved", "value": "5"
"Order": 13, "Id": "heading", "Name": "Heading", "Description": "True heading", "BitLength": 16, "BitOffset": 168, "BitStart": 0, "Units": "rad", "Resolution": "0.0001", "Signed": false
"Order": 14, "Id": "regionalApplication", "Name": "Regional Application", "BitLength": 8, "BitOffset": 184, "BitStart": 0, "Signed": false
"Order": 15, "Id": "regionalApplication", "Name": "Regional Application", "BitLength": 2, "BitOffset": 192, "BitStart": 0, "Signed": false
"Order": 16, "Id": "unitType", "Name": "Unit type", "BitLength": 1, "BitOffset": 194, "BitStart": 2, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "SOTDMA", "value": "0"
  "name": "CS", "value": "1"
"Order": 17, "Id": "integratedDisplay", "Name": "Integrated Display", "Description": "Whether the unit can show messages 12 and 14", "BitLength": 1, "BitOffset": 195, "BitStart": 3, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "No", "value": "0"
  "name": "Yes", "value": "1"
  "name": "Error", "value": "10"
  "name": "Unavailable", "value": "11"
"Order": 18, "Id": "dsc", "Name": "DSC", "BitLength": 1, "BitOffset": 196, "BitStart": 4, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "No", "value": "0"
  "name": "Yes", "value": "1"
  "name": "Error", "value": "10"
  "name": "Unavailable", "value": "11"
"Order": 19, "Id": "band", "Name": "Band", "BitLength": 1, "BitOffset": 197, "BitStart": 5, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "top 525 kHz of marine band", "value": "0"
  "name": "entire marine band", "value": "1"
"Order": 20, "Id": "canHandleMsg22", "Name": "Can handle Msg 22", "Description": "Whether device supports message 22", "BitLength": 1, "BitOffset": 198, "BitStart": 6, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "No", "value": "0"
  "name": "Yes", "value": "1"
  "name": "Error", "value": "10"
  "name": "Unavailable", "value": "11"
"Order": 21, "Id": "aisMode", "Name": "AIS mode", "BitLength": 1, "BitOffset": 199, "BitStart": 7, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "Autonomous", "value": "0"
  "name": "Assigned", "value": "1"
"Order": 22, "Id": "aisCommunicationState", "Name": "AIS communication state", "BitLength": 1, "BitOffset": 200, "BitStart": 0, "Type": "Lookup table", "Signed": false, "EnumValues": [
  "name": "SOTDMA", "value": "0"
  "name": "ITDMA", "value": "1"
     */
}