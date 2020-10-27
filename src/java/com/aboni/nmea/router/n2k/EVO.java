package com.aboni.nmea.router.n2k;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.impl.DefaultTimestampProvider;
import com.aboni.nmea.router.n2k.messages.impl.N2KGenericMessageImpl;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EVO {


    /*

    2020-10-26-10:48:07.788,3,126208,1,204,8,40,11,01,63,ff,00,f8,04
    2020-10-26-10:48:07.789,3,126208,1,204,8,41,01,3b,07,03,04,04,40
    2020-10-26-10:48:07.789,3,126208,1,204,8,42,00,05,ff,ff,ff,ff,ff

    2020-10-26-10:48:12.176,3,126208,1,204,8,c0,11,01,63,ff,00,f8,04
    2020-10-26-10:48:12.176,3,126208,1,204,8,c1,01,3b,07,03,04,04,00
    2020-10-26-10:48:12.177,3,126208,1,204,8,c2,00,05,ff,ff,ff,ff,ff

    01,63,ff,00,f8,04,01,3b,07,03,04,04,40,00,05,ff,ff,ff,ff,ff
    01,63,ff,00,f8,04,01,3b,07,03,04,04,00,00,05,ff,ff,ff,ff,ff

     */

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
                (byte)0xf8, // priority + reserved
                (byte)0x03, // n params
                (byte)0x01, (byte)0x3b, (byte)0x07, // param 1
                (byte)0x03, (byte)0x04, // param 2
                (byte)0x06, byte0, byte1, // param 3: heading
                (byte)0x00, (byte)0x05 // finish
        };

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), b);
    }

    public N2KMessage getWindDatumMessage(double windAngle) {

        windAngle = Utils.normalizeDegrees0To360(windAngle);
        long lWindAngle = Math.round(Math.toRadians(windAngle) / 0.0001);

        byte byte0 = (byte) (lWindAngle & 0xff);
        byte byte1 = (byte) (lWindAngle >> 8);

        byte[] b = new byte[] {
                (byte)0x01,
                (byte)0x50, // pgn 65345
                (byte)0xff, // pgn 65345
                (byte)0xf8, // priority + reserved
                (byte)0x03, // n params
                (byte)0x01, (byte)0x3b, (byte)0x07, // param 1
                (byte)0x03, (byte)0x04, // param 2
                (byte)0x04, byte0, byte1, // param 3: wind
                (byte)0x00, (byte)0x05 // finish
        };

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), b);
    }

    public N2KMessage getVANEMessage() {

        byte[] data = new byte[15];
        data[0] =        0x01;  // 126208 type (1 means "command")
        data[1] =        0x63;  // PGN
        data[2] =  (byte)0xff;  // PGN
        data[3] =        0x00;  // PGN
        data[4] =  (byte)0xf8;  // priority + reserved
        data[5] =        0x04;  // n params
        data[6] =        0x01;  // param 1 (manufacturer code)
        data[7] =        0x3b;  // 1851 Raymarine
        data[8] =        0x07;  // 1851 Raymarine
        data[9] =        0x03;  // parameter 3 (Industry code)
        data[10] =       0x04;  // Ind. code 4
        data[11] =       0x04;  // parameter 4 (mode)
        data[12] =       0x00;  // code 0 (VANE)
        data[13] =       0x05;  // parameter 5 (sub mode)
        data[14] =       0x01;  // sub code 1 (VANE)

        return new N2KGenericMessageImpl(new N2kHeader126208(126208), data);
    }

    /*
        {
      "PGN": 65379,
      "Id": "seatalkPilotMode",
      "Description": "Seatalk: Pilot Mode",
      "Type": "Single",
      "Complete": false,
      "Missing": [
        "Fields",
        "FieldLengths",
        "Precision"
      ],
      "Length": 8,
      "RepeatingFields": 0,
      "Fields": [
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
        {
          "Order": 7,
          "Id": "reserved",
          "Name": "Reserved",
          "BitLength": 24,
          "BitOffset": 40,
          "BitStart": 0,
          "Type": "Binary data",
          "Signed": false
        }
      ]
    },

     */

    static final Object p1 = new Object();
    static final Object p2 = new Object();


    public static void main(String[] args) {
        EVO evo = new EVO(new DefaultTimestampProvider());
        String s = "";
        for (byte i: evo.getAUTOMessage().getData())
            s += String.format("%02x", i);
        System.out.println(
                "message?pgn=126208&src=1&dest=204&priority=3&data=" + s);

        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (p2) {
                        p2.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("interrupted 1");
                }
                synchronized (p1) {
                    p1.notifyAll();
                }
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (p1) {
                        p1.wait();
                    }
                } catch (InterruptedException e) {
                    System.out.println("interrupted 2");
                }
                synchronized (p2) {
                    p2.notifyAll();
                }
            }
        });
        t1.start();
        t2.start();
        Utils.pause(1000);
        System.out.println(t1.getState().toString() + " " + t2.getState().toString());
        t1.interrupt();


        //GET /message?pgn=123456&dest=255&priority=6&data=A1A2A3A4A5A6A7A8 HTTP/1.1
    }
}
