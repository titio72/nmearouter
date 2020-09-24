package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2kMessagePGNs;

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

        public boolean isFast() {
            return fast;
        }
    }

    private static final Map<Integer, N2KDef> SUPPORTED = new HashMap<>();

    static {
        SUPPORTED.put(N2kMessagePGNs.WIND_PGN, N2KDef.getInstance(N2KWindDataImpl.class, false)); // Wind Data
        SUPPORTED.put(N2kMessagePGNs.DEPTH_PGN, N2KDef.getInstance(N2KWaterDepthImpl.class, false)); // Water Depth
        SUPPORTED.put(N2kMessagePGNs.SPEED_PGN, N2KDef.getInstance(N2KSpeedImpl.class, false)); // Speed
        SUPPORTED.put(N2kMessagePGNs.HEADING_PGN, N2KDef.getInstance(N2KHeadingImpl.class, false)); // Vessel Heading
        SUPPORTED.put(N2kMessagePGNs.POSITION_UPDATE_RAPID, N2KDef.getInstance(N2KPositionRapidImpl.class, false)); // Position, Rapid update
        SUPPORTED.put(N2kMessagePGNs.SOG_COG_RAPID_PGN, N2KDef.getInstance(N2KSOGAdCOGRapidImpl.class, false)); // COG & SOG, Rapid Update
        SUPPORTED.put(N2kMessagePGNs.GNSS_POSITION_UPDATE_PGN, N2KDef.getInstance(N2KGNSSPositionUpdateImpl.class, true)); // GNSS Pos update
        SUPPORTED.put(N2kMessagePGNs.GNSS_DOP_PGN, N2KDef.getInstance(N2KGNSSDOPsImpl.class, false)); // GNSS DOPs
        SUPPORTED.put(N2kMessagePGNs.SATELLITES_IN_VIEW_PGN, N2KDef.getInstance(N2KSatellitesImpl.class, true)); // List of sats
        SUPPORTED.put(N2kMessagePGNs.SYSTEM_TIME_PGN, N2KDef.getInstance(N2KSystemTimeImpl.class, false)); // System time
        SUPPORTED.put(N2kMessagePGNs.ATTITUDE_PGN, N2KDef.getInstance(N2KAttitudeImpl.class, false)); // Attitude)
        SUPPORTED.put(N2kMessagePGNs.ENVIRONMENT_130310_PGN, N2KDef.getInstance(N2KEnvironment310Impl.class, false)); // Env parameter: Water temp, air temp, pressure
        SUPPORTED.put(N2kMessagePGNs.ENVIRONMENT_130311_PGN, N2KDef.getInstance(N2KEnvironment311Impl.class, false)); // Env parameter: temperature, humidity, pressure
        SUPPORTED.put(N2kMessagePGNs.ENVIRONMENT_TEMPERATURE_PGN, N2KDef.getInstance(N2KTemperatureImpl.class, false)); // Env parameter: temperature
        SUPPORTED.put(N2kMessagePGNs.RUDDER_PGN, N2KDef.getInstance(N2KRudderImpl.class, false)); // Rudder
        SUPPORTED.put(N2kMessagePGNs.RATE_OF_TURN_PGN, N2KDef.getInstance(N2KRateOfTurnImpl.class, false)); // Rate of turn
        SUPPORTED.put(N2kMessagePGNs.SEATALK_PILOT_HEADING_PGN, N2KDef.getInstance(N2KSeatalkPilotHeadingImpl.class, false)); // Seatalk: Pilot Heading
        SUPPORTED.put(N2kMessagePGNs.SEATALK_PILOT_LOCKED_HEADING_PGN, N2KDef.getInstance(N2KSeatalkPilotLockedHeadingImpl.class, false)); // Seatalk: Pilot Locked Heading
        SUPPORTED.put(N2kMessagePGNs.SEATALK_PILOT_MODE_PGN, N2KDef.getInstance(N2KSeatalkPilotModeImpl.class, false)); // Seatalk: Pilot Mode
        SUPPORTED.put(N2kMessagePGNs.SEATALK_PILOT_WIND_DATUM_PGN, N2KDef.getInstance(N2KSeatalkPilotWindDatumImpl.class, false)); // Seatalk: wind datum
        SUPPORTED.put(N2kMessagePGNs.AIS_POSITION_REPORT_CLASS_A_PGN, N2KDef.getInstance(N2KAISPositionReportAImpl.class, true)); // AIS position report class A
        SUPPORTED.put(N2kMessagePGNs.AIS_POSITION_REPORT_CLASS_B_PGN, N2KDef.getInstance(N2KAISPositionReportBImpl.class, true)); // AIS Class B position report
        SUPPORTED.put(N2kMessagePGNs.AIS_POSITION_REPORT_CLASS_B_EXT_PGN, N2KDef.getInstance(N2KAISPositionReportBExt.class, true)); // AIS Class B position report
        SUPPORTED.put(N2kMessagePGNs.AIS_STATIC_DATA_CLASS_A_PGN, N2KDef.getInstance(N2KAISStaticDataAImpl.class, true)); // AIS Class A Static and Voyage Related Data
        SUPPORTED.put(N2kMessagePGNs.AIS_UTC_REPORT_PGN, N2KDef.getInstance(N2KAISUTCPositionReportImpl.class, true)); // AIS Position report in UTC
        SUPPORTED.put(N2kMessagePGNs.AIS_STATIC_DATA_CLASS_B_PART_A_PGN, N2KDef.getInstance(N2KAISStaticDataBPartAImpl.class, true)); // AIS Class B static data (msg 24 Part A)
        SUPPORTED.put(N2kMessagePGNs.AIS_STATIC_DATA_CLASS_B_PART_B_PGN, N2KDef.getInstance(N2KAISStaticDataBPartBImpl.class, true)); // AIS Class B static data (msg 24 Part B)
        SUPPORTED.put(N2kMessagePGNs.AIS_ATON_PGN, N2KDef.getInstance(N2KAISAtoN.class, true)); // AIS AtoN
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
