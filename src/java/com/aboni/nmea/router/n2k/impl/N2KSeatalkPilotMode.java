package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.SeatalkPilotMode;

public class N2KSeatalkPilotMode extends N2KMessageImpl {

    public static final int PGN = 65379;

    private SeatalkPilotMode mode;

    public N2KSeatalkPilotMode(byte[] data) {
        super(getDefaultHeader(PGN), data);
        fill();
    }

    public N2KSeatalkPilotMode(N2KMessageHeader header, byte[] data) throws PGNDataParseException {
        super(header, data);
        if (header == null) throw new PGNDataParseException("Null message header!");
        if (header.getPgn() != PGN)
            throw new PGNDataParseException(String.format("Incompatible header: expected %d, received %d", PGN, header.getPgn()));
        fill();
    }

    private void fill() {
        int m = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        int sm = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        int d = (int) parseIntegerSafe(data, 16, 0, 8, false, 0xFF);
        mode.setData(d);
        mode.setSubMode(sm);
        mode.setMode(m);
    }

    public SeatalkPilotMode.Mode getMode() {
        return mode.getPilotMode();
    }

    public void getMode(SeatalkPilotMode.Mode m) {
        mode.setPilotMode(m);
    }

    /*
    {
          "Order": 1,
          "Id": "manufacturerCode",
          "Name": "Manufacturer Code",
          "Description": "Raymarine",
          "BitLength": 11,
          "BitOffset": 0,
          "BitStart": 0,
          "Match": 1851,
          "Type": "Manufacturer code",
          "Signed": false
        },
        {
          "Order": 2,
          "Id": "reserved",
          "Name": "Reserved",
          "BitLength": 2,
          "BitOffset": 11,
          "BitStart": 3,
          "Resolution": 0,
          "Signed": false
        },
        {
          "Order": 3,
          "Id": "industryCode",
          "Name": "Industry Code",
          "Description": "Marine Industry",
          "BitLength": 3,
          "BitOffset": 13,
          "BitStart": 5,
          "Match": 4,
          "Type": "Lookup table",
          "Signed": false
        },
        {
          "Order": 4,
          "Id": "pilotMode",
          "Name": "Pilot Mode",
          "BitLength": 8,
          "BitOffset": 16,
          "BitStart": 0,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 5,
          "Id": "subMode",
          "Name": "Sub Mode",
          "BitLength": 8,
          "BitOffset": 24,
          "BitStart": 0,
          "Type": "Binary data",
          "Signed": false
        },
        {
          "Order": 6,
          "Id": "pilotModeData",
          "Name": "Pilot Mode Data",
          "BitLength": 8,
          "BitOffset": 32,
          "BitStart": 0,
          "Type": "Binary data",
          "Signed": false
        },
     */
}
