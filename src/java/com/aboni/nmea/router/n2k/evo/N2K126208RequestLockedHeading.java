package com.aboni.nmea.router.n2k.evo;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageImpl;

import java.time.Instant;

public class N2K126208RequestLockedHeading extends N2KMessageImpl {

    private final double lockedHeading;

    public N2K126208RequestLockedHeading(int src, Instant time, double lockedHeading) {
        super(new N2kHeader126208(src, time), getDataForMode(lockedHeading));
        this.lockedHeading = lockedHeading;
    }

    private static byte[] getDataForMode(double heading) {
        heading = Utils.normalizeDegrees0To360(heading);
        long lHeading = Math.round(Math.toRadians(heading) / 0.0001);

        byte byte0 = (byte) (lHeading & 0xff);
        byte byte1 = (byte) (lHeading >> 8);

        return new byte[] {
                (byte)0x01,
                (byte)0x50, // pgn 65360
                (byte)0xff, // pgn 65360
                (byte)0x00, // pgn 65360
                (byte)0xf8, // priority + reserved
                (byte)0x03, // n params
                (byte)0x01, (byte)0x3b, (byte)0x07, // param 1
                (byte)0x03, (byte)0x04, // param 2
                (byte)0x06, byte0, byte1 // param 3: heading
        };
    }

    public double getLockedHeading() {
        return lockedHeading;
    }
}
