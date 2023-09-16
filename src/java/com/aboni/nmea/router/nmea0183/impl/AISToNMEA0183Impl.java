package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.nmea.router.AISPositionReport;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.Message2NMEA0183;
import com.aboni.nmea.router.utils.Log;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;

public class AISToNMEA0183Impl implements Message2NMEA0183 {

    private final Log log;

    @Inject
    public AISToNMEA0183Impl(Log logger) {
        this.log = logger;
    }

    @Override
    public Sentence[] convert(Message message) {
        if (message instanceof AISPositionReport) {
            AISPositionReport ais = (AISPositionReport) message;
            if ("A".equals(ais.getAISClass())) {
                return handleAISPositionReportA(ais);
            } else if ("B".equals(ais.getAISClass())) {
                return handleAISPositionReportB(ais);
            } else {
                // TOD: log something
            }
        }


        return new Sentence[0];
    }

    private Sentence[] handleAISPositionReportB(AISPositionReport ais) {
        return null;
    }

    private Sentence[] handleAISPositionReportA(AISPositionReport ais) {
        /*
        int messageType = 1;
        int repeat = ais.getRepeatIndicator();
        int mmsi = Integer.parseInt(ais.getMMSI());
        int navigationStatus = ais.getNavStatus();
        double rateOfTurn,  // deg/sec
        double sog,         // Speed over ground is in 0.1-knot resolution from 0 to 102 knots. Value 1023 indicates speed is not available, value 1022 indicates 102.2 knots or higher.
        int accuracy,       // (0/1)
        double lon,         // deg
        double lat,         // deg
        double cog,         // deg
        double heading,     // deg
        int time,        // second from UTC (60 Not available, 61 pos is manual mode, 62 estimated pos, 63 position inoperative)
        int manouver         // 0/1/2
        ) {


            BitUtils bits = new BitUtils(c);
            bits.setNextInteger(messageType, 6);
            bits.setNextInteger(repeat, 2);
            bits.setNextInteger(mmsi, 30);
            bits.setNextInteger(navigationStatus, 4);
            bits.setNextInteger((int)(4.733 * Math.sqrt(rateOfTurn)), 8);
            bits.setNextInteger((int)(sog / 0.1), 10);
            bits.setNextInteger(accuracy, 1);
            bits.setNextInteger((int)Math.round(lon * 600000), 28);
            bits.setNextInteger((int)Math.round(lat * 600000), 27);
            bits.setNextInteger((int)Math.round(cog/ 0.1), 12);
            bits.setNextInteger((int)Math.round(heading), 9);
            bits.setNextInteger(time, 6);
            bits.setNextInteger(manouver, 2);
            bits.setNextInteger(0, 23);

            char[] data = new char[28];
            bits.setCurrentBitIndex(0);
            for (int i = 0; i<28; i++) {
                int v = bits.getNextInteger(6) & 0b00111111;
                if (v>=40) v += 8;
                char ch = (char)(v + 48);
                data[i] = ch;
                System.out.print(ch);
            }
            System.out.println();
            return new String(data);
        }

         */
        return null;
    }
}
