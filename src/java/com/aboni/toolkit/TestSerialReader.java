package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.CANDataFrame;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.SerialReader;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class TestSerialReader {

    private static final boolean DUMP_N2K = false;
    private static final String PORT_NAME = "COM8";
    private static final int SPEED = 115200;

    public static void onMsg(N2KMessage msg) {
        ConsoleLog.getLogger().info(msg.toString());
    }

    public static void onFrame(CANDataFrame msg) {
        ConsoleLog.getLogger().info(msg.toString());
    }

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        N2KFastCache cache = ThingsFactory.getInstance(N2KFastCache.class);
        SerialCANReader reader = ThingsFactory.getInstance(SerialCANReader.class, ConsoleLog.getLogger());
        SerialReader s = new SerialReader(ConsoleLog.getLogger());

        cache.setCallback(TestSerialReader::onMsg);
        reader.setCallback(cache::onMessage);
        if (DUMP_N2K) reader.setFrameCallback(TestSerialReader::onFrame);
        s.setup("Reader", PORT_NAME, SPEED, reader::onRead);
        s.activate();
    }
}
