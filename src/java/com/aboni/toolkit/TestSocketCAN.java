package com.aboni.toolkit;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.N2KFastCache;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.can.N2KHeader;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import tel.schich.javacan.CanChannels;
import tel.schich.javacan.CanFrame;
import tel.schich.javacan.NetworkDevice;
import tel.schich.javacan.RawCanChannel;
import tel.schich.javacan.linux.LinuxNativeOperationException;

import java.io.IOException;
import java.time.Duration;

import static tel.schich.javacan.CanSocketOptions.SO_RCVTIMEO;

public class TestSocketCAN {

    public static final String ERROR_READING_FRAME = "Error reading frame";

    public static void onMsg(N2KMessage msg) {
        ConsoleLog.getLogger().info(msg.toString());
    }

    public static void main(String... args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        N2KFastCache cache = ThingsFactory.getInstance(N2KFastCache.class);
        cache.setCallback(TestSerialReader::onMsg);
        RawCanChannel channel = null;
        try {
            Duration timeout = Duration.ofMillis(100);
            channel = CanChannels.newRawChannel();
            channel.bind(lookupDev());
            channel.setOption(SO_RCVTIMEO, timeout);
            ConsoleLog.getLogger().info("start");
            while (System.in.available() == 0) {
                readLoop(cache, channel);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void readLoop(N2KFastCache cache, RawCanChannel channel) {
        try {
            CanFrame frame = channel.read();
            byte[] b = new byte[frame.getDataLength()];
            frame.getData(b, 0, b.length);
            N2KMessageHeader h = new N2KHeader(frame.getId());
            N2KMessage msg = ThingsFactory.getInstance(N2KMessageFactory.class).newInstance(h, b);
            if (msg != null) cache.onMessage(msg);
        } catch (LinuxNativeOperationException e) {
            if (e.getErrorNumber() != 11) {
                ConsoleLog.getLogger().error(ERROR_READING_FRAME, e);
            }
        } catch (IOException e) {
            ConsoleLog.getLogger().error(ERROR_READING_FRAME, e);
        } catch (PGNDataParseException e) {
            ConsoleLog.getLogger().warning(ERROR_READING_FRAME, e);
        }
    }

    private static NetworkDevice lookupDev() {
        try {
            return NetworkDevice.lookup("vcan0");
        } catch (IOException e) {
            ConsoleLog.getLogger().warning("Error finding device", e);
        }
        return null;
    }

}
