package com.aboni.nmea.router.n2k;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.messages.impl.N2KGenericMessageImpl;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EVO {

    private final TimestampProvider tp;

    private class N2kHeader126208 implements N2KMessageHeader {

        private final Instant t;
        private final int pgn;

        N2kHeader126208(int pgn) {
            t = Instant.ofEpochMilli(tp.getNow());
            this.pgn = pgn;
        }

        @Override
        public int getPgn() {
            return pgn;
        }

        @Override
        public int getSource() {
            return 99;
        }

        @Override
        public int getDest() {
            return 204;
        }

        @Override
        public int getPriority() {
            return 3;
        }

        @Override
        public Instant getTimestamp() {
            return t;
        }
    }

    @Inject
    public EVO(@NotNull TimestampProvider tp) {
        this.tp = tp;
    }

    public N2KMessage getAUTOMessage() {

        byte[] data = new byte[] {
                (byte) 0x01,  // 126208 type (1 means "command")
                (byte) 0x63,  // PGN
                (byte) 0xff,  // PGN
                (byte) 0x00,  // PGN
                (byte) 0xf8,  // priority + reserved
                (byte) 0x04,  // n params
                (byte) 0x01,  // param 1 (manufacturer code)
                (byte) 0x3b,  // 1851 Raymarine
                (byte) 0x07,  // 1851 Raymarine
                (byte) 0x03,  // parameter 3 (Industry code)
                (byte) 0x04,  // Ind. code 4
                (byte) 0x04,  // parameter 4 (mode)
                (byte) 0x40,  // code 40 (AUTO)
                (byte) 0x00,  // finish
                (byte) 0x05   // finish
        };
        return new N2KGenericMessageImpl(new N2kHeader126208(126208), data);
    }

    public N2KMessage getSTDBYMessage() {

        byte[] data = new byte[] {
                (byte) 0x01,  // 126208 type (1 means "command")
                (byte) 0x63,  // PGN
                (byte) 0xff,  // PGN
                (byte) 0x00,  // PGN
                (byte) 0xf8,  // priority + reserved
                (byte) 0x04,  // n params
                (byte) 0x01,  // param 1 (manufacturer code)
                (byte) 0x3b,  // 1851 Raymarine
                (byte) 0x07,  // 1851 Raymarine
                (byte) 0x03,  // parameter 3 (Industry code)
                (byte) 0x04,  // Ind. code 4
                (byte) 0x04,  // parameter 4 (mode)
                (byte) 0x00,  // code 0 (STDBY)
                (byte) 0x00,  // finish
                (byte) 0x05   // finish
        };

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), data);
    }

    public N2KMessage getLockHeadingMessage(double heading) {

        heading = Utils.normalizeDegrees0To360(heading);
        long lHeading = Math.round(Math.toRadians(heading) / 0.0001);

        byte byte0 = (byte) (lHeading & 0xff);
        byte byte1 = (byte) (lHeading >> 8);

        byte[] b = new byte[] {
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

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), b);
    }

    public N2KMessage getWindDatumMessage(double windAngle) {

        windAngle = Utils.normalizeDegrees0To360(windAngle);
        long lWindAngle = Math.round(Math.toRadians(windAngle) / 0.0001);

        byte byte0 = (byte) (lWindAngle & 0xff);
        byte byte1 = (byte) (lWindAngle >> 8);

        //01 50 ff 00 f8 03 01 3b 07 03 04 06 08c5

        byte[] b = new byte[] {
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

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), b);
    }

    public N2KMessage getVANEMessage() {
        byte[] data = new byte[]{
                (byte) 0x01,  // 126208 type (1 means "command")
                (byte) 0x63,  // PGN
                (byte) 0xff,  // PGN
                (byte) 0x00,  // PGN
                (byte) 0xf8,  // priority + reserved
                (byte) 0x04,  // n params
                (byte) 0x01,  // param 1 (manufacturer code)
                (byte) 0x3b,  // 1851 Raymarine
                (byte) 0x07,  // 1851 Raymarine
                (byte) 0x03,  // parameter 3 (Industry code)
                (byte) 0x04,  // Ind. code 4
                (byte) 0x04,  // parameter 4 (mode)
                (byte) 0x00,  // code 0 (VANE)
                (byte) 0x01,  // parameter 1 (???? reverse engineered)
                (byte) 0x05 // finish
        };
        return new N2KGenericMessageImpl(new N2kHeader126208(126208), data);
    }
}
