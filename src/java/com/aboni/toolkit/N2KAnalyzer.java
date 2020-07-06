package com.aboni.toolkit;

import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.canboat.CANBOATDecoder;
import com.aboni.nmea.router.n2k.canboat.PGNParser;
import com.aboni.nmea.router.n2k.canboat.PGNs;
import com.aboni.nmea.router.n2k.canboat.impl.CANBOATDecoderImpl;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class N2KAnalyzer {

    private static boolean waitForMore;
    private static PGNParser pgnWaitingForMore;

    private static class QueuedPGN {
        PGNParser pgnParser;
        long lastSentence;
    }


    public static void main(String[] args) {
        try {
            PGNs pgNs = new PGNs("conf/pgns.json", null);
            CANBOATDecoder dec = new CANBOATDecoderImpl();

            try (FileReader r = new FileReader("nmea2000_1.log")) {
                BufferedReader bf = new BufferedReader(r);
                String line;
                while ((line = bf.readLine()) != null) {
                    try {
                        PGNParser p = new PGNParser(pgNs, line);
                        if (waitForMore && p.getPgn() == pgnWaitingForMore.getPgn()) {
                            pgnWaitingForMore.addMore(line);
                            if (!pgnWaitingForMore.needMore()) {
                                p = pgnWaitingForMore;
                                pgnWaitingForMore = null;
                                waitForMore = false;
                            } else {
                                p = null;
                            }
                        }
                        if (p != null) {
                            if (p.needMore()) {
                                waitForMore = true;
                                pgnWaitingForMore = p;
                            } else {
                                System.out.println("\n------------------------------------------- Multi " + (p.getLength() > 8));
                                System.out.println(line);
                                JSONObject j = p.getCanBoatJson();
                                if (j != null) {
                                    Sentence[] nmeas = dec.getSentence(j);
                                    if (nmeas != null) {
                                        for (Sentence ss : dec.getSentence(j)) {
                                            if (ss != null) System.out.println(ss);
                                        }
                                    }
                                }
                                System.out.println(j);
                            }
                        }
                    } catch (PGNDataParseException e) {
                        // do nothing
                    } catch (Exception e) {
                        System.out.printf("Error {%s}%n", line);
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
