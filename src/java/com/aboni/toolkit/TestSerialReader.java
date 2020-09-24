package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.N2KCanReader;
import com.aboni.nmea.router.n2k.can.N2KFastCache;
import com.aboni.utils.SerialReader;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSerialReader {

    private static final String PORT_NAME = "/dev/ttyUSB0";
    private static final int SPEED = 115200;

    public static void onMsg(N2KMessage msg) {
        if (msg.getHeader().getPgn() == 129540)
            System.out.println(msg);
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        SerialReader s = new SerialReader();
        N2KFastCache cache = new N2KFastCache(null);
        cache.setCallback(TestSerialReader::onMsg);
        N2KCanReader reader = new N2KCanReader(cache::onMessage);
        //N2KCanReader reader = new N2KCanReader(TestSerialReader::onMsg);
        s.setup(PORT_NAME, SPEED, reader::onRead);
        s.activate();
    }
}
