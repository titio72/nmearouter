package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.N2KHeader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.BufferedReader;
import java.io.FileReader;

public class N2KAnalyzer {

    public static void onMsg(N2KMessage msg) {
        ConsoleLog.getLogger().console(msg.toString());
    }

    public static void main(String... args) {
        try (FileReader fileReader = new FileReader("/home/aboni/Downloads/NMEA2000/p.txt")) {
            BufferedReader reader = new BufferedReader(fileReader);
            Injector injector = Guice.createInjector(new NMEARouterModule());
            ThingsFactory.setInjector(injector);
            N2KFastCache cache = ThingsFactory.getInstance(N2KFastCache.class);
            cache.setCallback(N2KAnalyzer::onMsg);

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
                N2KMessage msg = ThingsFactory.getInstance(N2KMessageFactory.class).newUntypedInstance(iso, data);
                if (ThingsFactory.getInstance(N2KMessageFactory.class).isSupported(iso.getPgn())) {
                    cache.onMessage(msg);
                } else {
                    onMsg(msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
