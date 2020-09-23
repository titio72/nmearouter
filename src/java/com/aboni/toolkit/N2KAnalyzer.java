package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.N2KFastCache;
import com.aboni.nmea.router.n2k.can.N2KHeader;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefaultImpl;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefinitions;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.BufferedReader;
import java.io.FileReader;

public class N2KAnalyzer {

    private static int srcFilter = -1;

    public static void onMsg(N2KMessage msg) {
        if (srcFilter == -1 || msg.getHeader().getSource() == srcFilter) System.out.println(msg);
    }

    public static void main(String... args) {
        try (FileReader fileReader = new FileReader("/home/aboni/Downloads/NMEA2000/p.txt")) {
            BufferedReader reader = new BufferedReader(fileReader);
            Injector injector = Guice.createInjector(new NMEARouterModule());
            ThingsFactory.setInjector(injector);
            N2KFastCache cache = new N2KFastCache(N2KAnalyzer::onMsg);

            String line;
            while ((line = reader.readLine()) != null) {
                // frame 09f10dcc [8] fc f8 ff 7f ff 7f ff ff
                String[] tokens = line.split(" ");
                long id = Long.parseLong(tokens[1], 16);
                int l = Integer.parseInt(tokens[2].substring(1, tokens[2].length() - 1));
                byte[] data = new byte[l];
                for (int i = 0; i < l; i++) {
                    data[i] = (byte) (Integer.parseInt(tokens[3 + i], 16) & 0xFF);
                }
                N2KHeader iso = new N2KHeader(id);
                N2KMessageDefaultImpl msg = new N2KMessageDefaultImpl(iso, data);
                if (N2KMessageDefinitions.isSupported(iso.getPgn())) {
                    //if (SRC==-1 || msg.getHeader().getSource()==SRC) System.out.println(msg);
                    cache.onMessage(msg);
                } else {
                    //onMsg(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
