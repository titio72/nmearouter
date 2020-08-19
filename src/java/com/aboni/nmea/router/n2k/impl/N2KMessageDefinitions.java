package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("OverlyCoupledClass")
public class N2KMessageDefinitions {

    private N2KMessageDefinitions() {
    }

    public static class N2KDef {
        Class<? extends N2KMessage> messageClass;
        boolean fast;
        Constructor<?> constructor;
        Constructor<?> constructor1;

        static N2KDef getInstance(Class<? extends N2KMessage> c, boolean fast) {
            N2KDef d = new N2KDef();
            d.fast = fast;
            d.messageClass = c;
            try {
                d.constructor = c.getConstructor(N2KMessageHeader.class, byte[].class);
            } catch (NoSuchMethodException e) {
                d.constructor = null;
            }
            try {
                d.constructor1 = c.getConstructor(byte[].class);
            } catch (NoSuchMethodException e) {
                d.constructor1 = null;
            }
            return d;
        }
    }

    private static final Map<Integer, N2KDef> SUPPORTED = new HashMap<>();

    static {
        SUPPORTED.put(130306, N2KDef.getInstance(N2KWindDataImpl.class, false)); // Wind Data
        SUPPORTED.put(128267, N2KDef.getInstance(N2KWaterDepthImpl.class, false)); // Water Depth
        SUPPORTED.put(128259, N2KDef.getInstance(N2KSpeedImpl.class, false)); // Speed
        SUPPORTED.put(127250, N2KDef.getInstance(N2KHeadingImpl.class, false)); // Vessel Heading
        SUPPORTED.put(129025, N2KDef.getInstance(N2KPositionRapidImpl.class, false)); // Position, Rapid update
        SUPPORTED.put(129026, N2KDef.getInstance(N2KSOGAdCOGRapidImpl.class, false)); // COG & SOG, Rapid Update
        SUPPORTED.put(129029, N2KDef.getInstance(N2KGNSSPositionUpdateImpl.class, true)); // GNSS Pos update
        SUPPORTED.put(129540, N2KDef.getInstance(N2KSatellitesImpl.class, true)); // List of sats
        SUPPORTED.put(126992, N2KDef.getInstance(N2KSystemTimeImpl.class, false)); // System time
        SUPPORTED.put(127257, N2KDef.getInstance(N2KAttitudeImpl.class, false)); // Attitude)
        SUPPORTED.put(130310, N2KDef.getInstance(N2KEnvironment310Impl.class, false)); // Env parameter: Water temp, air temp, pressure
        SUPPORTED.put(130311, N2KDef.getInstance(N2KEnvironment311Impl.class, false)); // Env parameter: temperature, humidity, pressure
        SUPPORTED.put(127245, N2KDef.getInstance(N2KRudderImpl.class, false)); // Rudder
        SUPPORTED.put(127251, N2KDef.getInstance(N2KRateOfTurnImpl.class, false)); // Rate of turn
        SUPPORTED.put(65359, N2KDef.getInstance(N2KSeatalkPilotHeadingImpl.class, false)); // Seatalk: Pilot Heading
        SUPPORTED.put(65360, N2KDef.getInstance(N2KSeatalkPilotLockedHeadingImpl.class, false)); // Seatalk: Pilot Locked Heading
        SUPPORTED.put(65379, N2KDef.getInstance(N2KSeatalkPilotModeImpl.class, false)); // Seatalk: Pilot Mode
        SUPPORTED.put(65345, N2KDef.getInstance(N2KSeatalkPilotWindDatumImpl.class, false)); // Seatalk: wind datum
        SUPPORTED.put(129038, N2KDef.getInstance(N2KAISPositionReportAImpl.class, true)); // AIS position report class A
        SUPPORTED.put(129039, N2KDef.getInstance(N2KAISPositionReportBImpl.class, true)); // AIS Class B position report
        SUPPORTED.put(129040, N2KDef.getInstance(N2KAISPositionReportBExt.class, true)); // AIS Class B position report
        SUPPORTED.put(129794, N2KDef.getInstance(N2KAISStaticDataAImpl.class, true)); // AIS Class A Static and Voyage Related Data
        SUPPORTED.put(129809, N2KDef.getInstance(N2KAISStaticDataBPartAImpl.class, true)); // AIS Class B static data (msg 24 Part A)
        SUPPORTED.put(129810, N2KDef.getInstance(N2KAISStaticDataBPartBImpl.class, true)); // AIS Class B static data (msg 24 Part B)
    }

    public static boolean isSupported(int pgn) {
        return SUPPORTED.containsKey(pgn);
    }

    public static N2KDef getDefinition(int pgn) {
        return SUPPORTED.getOrDefault(pgn, null);
    }

    public static N2KMessage newInstance(int pgn, N2KMessageHeader h, byte[] data) throws PGNDataParseException {
        N2KDef def = getDefinition(pgn);
        if (def != null && def.constructor != null) {
            try {
                return (N2KMessage) def.constructor.newInstance(h, data);
            } catch (Exception e) {
                throw new PGNDataParseException(e);
            }
        }
        return null;
    }

    public static N2KMessage newInstance(int pgn, byte[] data) throws PGNDataParseException {
        N2KDef def = getDefinition(pgn);
        if (def != null && def.constructor1 != null) {
            try {
                //noinspection PrimitiveArrayArgumentToVarargsMethod
                return (N2KMessage) def.constructor1.newInstance(data);
            } catch (Exception e) {
                throw new PGNDataParseException(e);
            }
        }
        return null;
    }
}
