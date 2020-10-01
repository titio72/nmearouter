package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.SerialReader;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSerialReader {

    private static final String PORT_NAME = "COM10";
    private static final int SPEED = 115200;

    public static void onMsg(N2KMessage msg) {
        ConsoleLog.getLogger().console(msg.toString());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        SerialReader s = new SerialReader(ConsoleLog.getLogger());
        N2KFastCache cache = ThingsFactory.getInstance(N2KFastCache.class);
        cache.setCallback(TestSerialReader::onMsg);
        SerialCANReader reader = ThingsFactory.getInstance(SerialCANReader.class, ConsoleLog.getLogger());
        reader.setCallback(TestSerialReader::onMsg);
        s.setup(PORT_NAME, SPEED, reader::onRead);
        s.activate();
    }
}
