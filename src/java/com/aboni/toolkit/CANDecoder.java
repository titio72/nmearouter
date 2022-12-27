package com.aboni.toolkit;


import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.can.CANDataFrame;
import com.aboni.nmea.router.n2k.can.SerialCANReader;
import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class CANDecoder {

    private static final int[] frame = new int[] {0xaa, 0xe8, 0x02,  0x01, 0xf8, 0x09, 0x7a, 0xa8, 0x08, 0x1a, 0xaa, 0xe8, 0x02, 0x0b, 0xf9, 0x0d, 0xff, 0xfc, 0x2c, 0x1d, 0x08, 0x00, 0xff, 0xff, 0x55, 0xaa};

    public static void main(String... args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        SerialCANReader reader = ThingsFactory.getInstance(SerialCANReader.class, ConsoleLog.getLogger());
        reader.setCallback(CANDecoder::onRead);
        reader.setErrCallback(CANDecoder::onError);
        reader.setFrameCallback(CANDecoder::onFrame);

        reader.onRead(frame, frame.length - 1);
    }

    private static void onFrame(CANDataFrame frame) {
        ConsoleLog.getLogger().info(frame.toString());
    }


    private static void onRead(N2KMessage n2KMessage) {
        ConsoleLog.getLogger().info(n2KMessage.toString());
    }

    private static void onError(byte[] bytes, String error) {
        ConsoleLog.getLogger().info(error);
    }

}
