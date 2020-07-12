package com.aboni.toolkit;

import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class N2KAnalyzer {

    private static boolean waitForMore;
    private static N2KMessageParser pgnWaitingForMore;

    private static class QueuedPGN {
        N2KMessageParser pgnParser;
        long lastSentence;
    }


    public static void main(String[] args) {
        try {
            try (FileReader r = new FileReader("nmea2000_1.log")) {
                BufferedReader bf = new BufferedReader(r);
                String line;
                while ((line = bf.readLine()) != null) {
                    try {
                        N2KMessageParser p = new N2KMessageParser(line);
                        if (waitForMore && p.getHeader().getPgn() == pgnWaitingForMore.getHeader().getPgn()) {
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
                                System.out.println(p.toString());
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
