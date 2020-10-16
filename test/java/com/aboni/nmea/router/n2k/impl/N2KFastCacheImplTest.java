package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class N2KFastCacheImplTest {

    private N2KFastCache cache;
    private N2KMessageFactory factory;

    private class MyH implements N2KMessageHeader {

        @Override
        public int getPgn() {
            return 129029;
        }

        @Override
        public int getSource() {
            return 22;
        }

        @Override
        public int getDest() {
            return 255;
        }

        @Override
        public int getPriority() {
            return 2;
        }

        @Override
        public Instant getTimestamp() {
            return Instant.now();
        }
    }

    @Before
    public void setUp() {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);
        cache = ThingsFactory.getInstance(N2KFastCache.class);
        factory = ThingsFactory.getInstance(N2KMessageFactory.class);
    }

    @Test
    public void test() throws PGNDataParseException {
        final AtomicInteger i = new AtomicInteger();
        cache.setCallback(new N2KMessageCallback() {
            @Override
            public void onMessage(N2KMessage msg) {
                System.out.println(msg);
                assertEquals(7, i.get());
            }
        });
        N2KMessage[] msgs = new N2KMessage[]{
                factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x00, (byte) 0x2b, (byte) 0x45, (byte) 0x70, (byte) 0x48, (byte) 0x50, (byte) 0x77, (byte) 0xd5})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x01, (byte) 0x2b, (byte) 0x00, (byte) 0x31, (byte) 0xe6, (byte) 0xb2, (byte) 0xbc, (byte) 0xbc})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x02, (byte) 0x0f, (byte) 0x06, (byte) 0x80, (byte) 0xd5, (byte) 0xc4, (byte) 0x57, (byte) 0x28})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x03, (byte) 0x01, (byte) 0x6d, (byte) 0x01, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x04, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0x7f, (byte) 0x10, (byte) 0xfd, (byte) 0x11})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x05, (byte) 0x41, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00})
                ,factory.newUntypedInstance(new MyH(), new byte[]{(byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff})
                };
        for (N2KMessage m: msgs) {
            i.incrementAndGet();
            cache.onMessage(m);
        }
    }
}