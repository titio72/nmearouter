package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.EvoAutoPilotStatus;
import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHandler;
import com.aboni.nmea.router.n2k.evo.N2K126208RequestLockedHeading;
import com.aboni.nmea.router.n2k.evo.N2K126208RequestPilotMode;
import com.aboni.nmea.router.n2k.evo.N2K126208RequestWindDatum;
import com.aboni.toolkit.ProgrammableTimeStampProvider;
import com.aboni.utils.ConsoleLog;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class EvoAPDriverTest {

    private static class MyAPStatus implements EvoAutoPilotStatus {

        double apHeading = Double.NaN;
        double apLockedHeading = Double.NaN;
        double windDatum = Double.NaN;
        double windAverage = Double.NaN;
        PilotMode mode = PilotMode.STANDBY;

        @Override
        public void listen(PilotStatusListener listener) {}

        @Override
        public void stopListening(PilotStatusListener listener) {}

        @Override
        public double getApHeading() {
            return apHeading;
        }

        @Override
        public double getApLockedHeading() {
            return apLockedHeading;
        }

        @Override
        public double getApWindDatum() {
            return windDatum;
        }

        @Override
        public double getApAverageWind() {
            return windAverage;
        }

        @Override
        public PilotMode getMode() {
            return mode;
        }

        public void setApLockedHeading(double apLockedHeading) {
            this.apLockedHeading = apLockedHeading;
        }

        public void setWindDatum(double windDatum) {
            this.windDatum = windDatum;
        }

        public void setWindAverage(double windAverage) {
            this.windAverage = windAverage;
        }

        public void setMode(PilotMode mode) {
            this.mode = mode;
        }

        void reset() {
            apLockedHeading = Double.NaN;
            apHeading = 25;
            windAverage = Double.NaN;
            windDatum = Double.NaN;
            mode = PilotMode.STANDBY;
        }
    }

    private static class MySender implements N2KMessageHandler {

        List<N2KMessage> list = new ArrayList<>();

        @Override
        public void onMessage(N2KMessage msg) {
            list.add(msg);
        }

        List<N2KMessage> getList() {
            return list;
        }

        void reset() {
            list.clear();
        }
    }

    private final MySender sender = new MySender();
    private final MyAPStatus status = new MyAPStatus();
    private final ProgrammableTimeStampProvider tp = new ProgrammableTimeStampProvider();
    private final EvoAPDriver driver = new EvoAPDriver(ConsoleLog.getLogger(), status, tp, sender);

    @Before
    public void before() {
        status.reset();
        sender.reset();
    }

    @Test
    public void testSwitchMode() {
        before(); testSwitchMode(PilotMode.STANDBY, PilotMode.AUTO);
        before(); testSwitchMode(PilotMode.AUTO, PilotMode.AUTO);
        before(); testSwitchMode(PilotMode.AUTO, PilotMode.STANDBY);
        before(); testSwitchMode(PilotMode.STANDBY, PilotMode.VANE);
        before(); testSwitchMode(PilotMode.AUTO, PilotMode.VANE);
        before(); testSwitchMode(PilotMode.VANE, PilotMode.AUTO);
        before(); testSwitchMode(PilotMode.VANE, PilotMode.STANDBY);
    }

    void testSwitchMode(PilotMode from, PilotMode to) {
        status.setMode(from);
        tp.setTimestamp(System.currentTimeMillis());
        switch (to) {
            case STANDBY: driver.setStandby(); break;
            case AUTO: driver.setAuto(); break;
            case VANE: driver.setWindVane(); break;
            default: fail("Unsupported mode " + to);
        }
        assertEquals(1, sender.getList().size());
        assertEquals(to, ((N2K126208RequestPilotMode)sender.getList().get(0)).getMode());
    }

    @Test
    public void testIncrHeading1AUTO() {
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard1();
        assertEquals(1, sender.getList().size());
        assertEquals(23, ((N2K126208RequestLockedHeading)sender.getList().get(0)).getLockedHeading(), 0.01);
    }

    @Test
    public void testDecrHeading1AUTO() {
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.port1();
        assertEquals(1, sender.getList().size());
        assertEquals(21, ((N2K126208RequestLockedHeading)sender.getList().get(0)).getLockedHeading(), 0.01);
    }

    @Test
    public void testDecrHeading10AUTO() {
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.port10();
        assertEquals(1, sender.getList().size());
        assertEquals(12, ((N2K126208RequestLockedHeading)sender.getList().get(0)).getLockedHeading(), 0.01);
    }

    @Test
    public void testIncrHeading10AUTO() {
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard10();
        assertEquals(1, sender.getList().size());
        assertEquals(32, ((N2K126208RequestLockedHeading)sender.getList().get(0)).getLockedHeading(), 0.01);
    }

    @Test
    public void testIncrHeading20AUTO() {
        // click quickly twice - second click should start from the value set by the first click instead of from the status
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard10();
        status.setApLockedHeading(31); // 1 less than the expected - if the next click uses this value, the test fails
        driver.starboard10();
        assertEquals(2, sender.getList().size());
        assertEquals(42, ((N2K126208RequestLockedHeading)sender.getList().get(1)).getLockedHeading(), 0.01);
    }

    @Test
    public void testIncrHeading20AUTOSlow() {
        // click slowly twice - second click will use the actual lockedheading and add 10
        status.setMode(PilotMode.AUTO);
        status.setApLockedHeading(22);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard10();
        status.setApLockedHeading(31); // 1 less than the expected
        tp.setTimestamp(tp.getNow() + 750); // half a second delay between the two clicks
        driver.starboard10();
        assertEquals(2, sender.getList().size());
        assertEquals(41, ((N2K126208RequestLockedHeading)sender.getList().get(1)).getLockedHeading(), 0.01);
    }

    @Test
    public void testHeadUpOnStarboardSide() {
        status.setMode(PilotMode.VANE);
        status.setWindDatum(48);
        status.setWindAverage(49);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard10();
        assertEquals(1, sender.getList().size());
        assertEquals(38, ((N2K126208RequestWindDatum)sender.getList().get(0)).getWindDatum(), 0.01);
    }

    @Test
    public void testHeadUpOnPortSide() {
        status.setMode(PilotMode.VANE);
        status.setWindDatum(312);
        status.setWindAverage(311);
        tp.setTimestamp(System.currentTimeMillis());
        driver.port10();
        assertEquals(1, sender.getList().size());
        assertEquals(322, ((N2K126208RequestWindDatum)sender.getList().get(0)).getWindDatum(), 0.01);
    }

    @Test
    public void testBearAwayOnPortSide() {
        status.setMode(PilotMode.VANE);
        status.setWindDatum(312);
        status.setWindAverage(311);
        tp.setTimestamp(System.currentTimeMillis());
        driver.starboard10();
        assertEquals(1, sender.getList().size());
        assertEquals(302, ((N2K126208RequestWindDatum)sender.getList().get(0)).getWindDatum(), 0.01);
    }

    @Test
    public void testBearAwayOnStarboardSide() {
        status.setMode(PilotMode.VANE);
        status.setWindDatum(48);
        status.setWindAverage(49);
        tp.setTimestamp(System.currentTimeMillis());
        driver.port10();
        assertEquals(1, sender.getList().size());
        assertEquals(58, ((N2K126208RequestWindDatum)sender.getList().get(0)).getWindDatum(), 0.01);
    }

}