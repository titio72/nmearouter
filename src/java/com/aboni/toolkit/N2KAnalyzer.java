package com.aboni.toolkit;

import com.aboni.nmea.router.n2k.CANBOATDecoder;
import com.aboni.nmea.router.n2k.PGNParser;
import com.aboni.nmea.router.n2k.PGNs;
import com.aboni.nmea.router.n2k.impl.CANBOATDecoderImpl;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class N2KAnalyzer {

    private static boolean waitForMore;
    private static PGNParser pgnWaitingForMore;

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
                                    System.out.println(dec.getSentence(j));
                                }
                                System.out.println(j);
                            }
                        }
                    } catch (PGNParser.PGNDataParseException e) {
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
