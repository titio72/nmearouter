package com.aboni.nmea.router.n2k.evo;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageImpl;

import java.time.Instant;

public class N2K126208RequestWindDatum extends N2KMessageImpl {

    private final double windDatum;

    public N2K126208RequestWindDatum(int src, Instant time, double windDatum) {
        super(new N2kHeader126208(src, time), getDataForMode(windDatum));
        this.windDatum = windDatum;
    }

    private static byte[] getDataForMode(double windDatum) {
        windDatum = Utils.normalizeDegrees0To360(windDatum);
        long lWindAngle = Math.round(Math.toRadians(windDatum) / 0.0001);

        byte byte0 = (byte) (lWindAngle & 0xff);
        byte byte1 = (byte) (lWindAngle >> 8);

        return new byte[] {
                (byte)0x01,
                (byte)0x41, // pgn 65345
                (byte)0xff, // pgn 65345
                (byte)0x00, // pgn 65345
                (byte)0xf8, // priority + reserved
                (byte)0x03, // n params
                (byte)0x01, (byte)0x3b, (byte)0x07, // param 1
                (byte)0x03, (byte)0x04, // param 2
                (byte)0x04, byte0, byte1 // param 3: wind
        };
    }

    public double getWindDatum() {
        return windDatum;
    }
}
