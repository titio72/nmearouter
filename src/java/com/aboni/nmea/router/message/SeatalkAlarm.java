package com.aboni.nmea.router.message;

public enum SeatalkAlarm {

    NO_ALARM("NoAlarm",0),
    SHALLOW_DEPTH("ShallowDepth",1),
    DEEP_DEPTH("DeepDepth",2),
    SHALLOW_ANCHOR("ShallowAnchor",3),
    DEEP_ANCHOR("DeepAnchor",4),
    OFF_COURSE("OffCourse",5),
    AWA_HIGH("AWAHigh",6),
    AWA_LOW("AWALow",7),
    AWS_HIGH("AWSHigh",8),
    AWS_LOW("AWSLow",9),
    TWA_HIGH("TWAHigh",10),
    TWA_LOW("TWALow",11),
    TWS_HIGH("TWSHigh",12),
    TWS_LOW("TWSLow",13),
    WP_ARRIVAL("WPArrival",14),
    BOAT_SPEED_HIGH("BoatSpeedHigh",15),
    BOAT_SPEED_LOW("BoatSpeedLow",16),
    SEA_TEMP_HIGH("SeaTempHigh",17),
    SEA_TEMP_LOW("SeaTempLow",18),
    PILOT_WATCH("PilotWatch",19),
    PILOT_OFF_COURSE("PilotOffCourse",20),
    PILOT_WIND_SHIFT("PilotWindShift",21),
    PILOT_LOW_BATTERY("PilotLowBattery",22),
    PILOT_LAST_MINUTE_OF_WATCH("PilotLastMinuteOfWatch",23),
    PILOT_NO_NMEA_DATA("PilotNoNMEAData",24),
    PILOT_LARGE_XTE("PilotLargeXTE",25),
    PILOT_NMEA_DATAERROR("PilotNMEADataError",26),
    PILOT_CU_DISCONNECTED("PilotCUDisconnected",27),
    PILOT_AUTO_RELEASE("PilotAutoRelease",28),
    PILOT_WAY_POINT_ADVANCE("PilotWayPointAdvance",29),
    PILOT_DRIVE_STOPPED("PilotDriveStopped",30),
    PILOT_TYPE_UNSPECIFIED("PilotTypeUnspecified",31),
    PILOT_CALIBRATION_REQUIRED("PilotCalibrationRequired",32),
    PILOT_LAST_HEADING("PilotLastHeading",33),
    PILOT_NO_PILOT("PilotNoPilot",34),
    PILOT_ROUTE_COMPLETE("PilotRouteComplete",35),
    PILOT_VARIABLE_TEXT("PilotVariableText",36),
    GPS_FAILURE("GPSFailure",37),
    MOB("MOB",38),
    SEATALK1_ANCHOR("Seatalk1Anchor",39),
    PILOT_SWAPPED_MOTOR_POWER("PilotSwappedMotorPower",40),
    PILOT_STANDBY_TOO_FAST_TO_FISH("PilotStandbyTooFastToFish",41),
    PILOT_NO_GPS_FIX("PilotNoGPSFix",42),
    PILOT_NO_GPS_COG("PilotNoGPSCOG",43),
    PILOT_START_UP("PilotStartUp",44),
    PILOT_TOO_SLOW("PilotTooSlow",45),
    PILOT_NO_COMPASS("PilotNoCompass",46),
    PILOT_RATE_GYRO_FAULT("PilotRateGyroFault",47),
    PILOT_CURRENT_LIMIT("PilotCurrentLimit",48),
    PILOT_WAY_POINT_ADVANCE_PORT("PilotWayPointAdvancePort",49),
    PILOT_WAY_POINT_ADVANCE_STBD("PilotWayPointAdvanceStbd",50),
    PILOT_NO_WIND_DATA("PilotNoWindData",51),
    PILOT_NO_SPEED_DATA("PilotNoSpeedData",52),
    PILOT_SEATALK_FAIL1("PilotSeatalkFail1",53),
    PILOT_SEATALK_FAIL2("PilotSeatalkFail2",54),
    PILOT_WARNING_TOO_FAST_TO_FISH("PilotWarningTooFastToFish",55),
    PILOT_AUTO_DOCKSIDE_FAIL("PilotAutoDocksideFail",56),
    PILOT_TURN_TOO_FAST("PilotTurnTooFast",57),
    PILOT_NO_NAV_DATA("PilotNoNavData",58),
    PILOT_LOST_WAYPOINT_DATA("PilotLostWaypointData",59),
    PILOT_EEPROM_CORRUPT("PilotEEPROMCorrupt",60),
    PILOT_RUDDER_FEEDBACK_FAIL("PilotRudderFeedbackFail",61),
    PILOT_AUTOLEARN_FAIL1("PilotAutolearnFail1",62),
    PILOT_AUTOLEARN_FAIL2("PilotAutolearnFail2",63),
    PILOT_AUTOLEARN_FAIL3("PilotAutolearnFail3",64),
    PILOT_AUTOLEARN_FAIL4("PilotAutolearnFail4",65),
    PILOT_AUTOLEARN_FAIL5("PilotAutolearnFail5",66),
    PILOT_AUTOLEARN_FAIL6("PilotAutolearnFail6",67),
    PILOT_WARNING_CAL_REQUIRED("PilotWarningCalRequired",68),
    PILOT_WARNING_OFFCOURSE("PilotWarningOffCourse",69),
    PILOT_WARNING_XTE("PilotWarningXTE",70),
    PILOT_WARNING_WIND_SHIFT("PilotWarningWindShift",71),
    PILOT_WARNING_DRIVE_SHORT("PilotWarningDriveShort",72),
    PILOT_WARNING_CLUTCH_SHORT("PilotWarningClutchShort",73),
    PILOT_WARNING_SOLENOID_SHORT("PilotWarningSolenoidShort",74),
    PILOT_JOYSTICK_FAULT("PilotJoystickFault",75),
    PILOT_NO_JOYSTICK_DATA("PilotNoJoystickData",76),
    NOT_ASSIGNED1("notassigned1",77),
    NOT_ASSIGNED2("notassigned2",78),
    NOT_ASSIGNED3("notassigned3",79),
    PILOT_INVALID_COMMAND("PilotInvalidCommand",80),
    AIS_TX_MALFUNCTION("AISTXMalfunction",81),
    AIS_ANTENNA_VSWR_FAULT("AISAntennaVSWRfault",82),
    AIS_RX_CHANNEL_1_MALFUNCTION("AISRxchannel1malfunction",83),
    AIS_RX_CHANNEL_2_MALFUNCTION("AISRxchannel2malfunction",84),
    AIS_NO_SENSOR_POSITION_IN_USE("AISNosensorpositioninuse",85),
    AIS_NO_VALID_SOG_INFORMATION("AISNovalidSOGinformation",86),
    AIS_NO_VALID_COG_INFORMATION("AISNovalidCOGinformation",87),
    AIS_12V_ALARM("AIS12Valarm",88),
    AIS_6V_ALARM("AIS6Valarm",89),
    AIS_NOISE_THRESHOLD_EXCEEDED_CHANNEL_A("AISNoisethresholdexceededchannelA",90),
    AIS_NOISE_THRESHOLD_EXCEEDED_CHANNEL_B("AISNoisethresholdexceededchannelB",91),
    AIS_TRANSMITTER_PA_FAULT("AISTransmitterPAfault",92),
    AIS_3V3_ALARM("AIS3V3alarm",93),
    AIS_RX_CHANNEL_70_MALFUNCTION("AISRxchannel70malfunction",94),
    AIS_HEADING_LOST_INVALID("AISHeadinglost/invalid",95),
    AIS_INTERNAL_GPS_LOST("AISinternalGPSlost",96),
    AIS_NO_SENSOR_POSITION("AISNosensorposition",97),
    AIS_LOCK_FAILURE("AISLockfailure",98),
    AIS_INTERNAL_GGA_TIMEOUT("AISInternalGGAtimeout",99),
    AIS_PROTOCOL_STACK_RESTART("AISProtocolstackrestart",100),
    PILOT_NO_IPS_COMMUNICATIONS("PilotNoIPScommunications",101),
    PILOT_POWER_ON_OR_SLEEP_SWITCH_RESET_WHILE_ENGAGED("PilotPower-OnorSleep-SwitchResetWhileEngaged",102),
    PILOT_UNEXPECTED_RESET_WHILE_ENGAGED("PilotUnexpectedResetWhileEngaged",103),
    AIS_DANGEROUS_TARGET("AISDangerousTarget",104),
    AIS_LOST_TARGET("AISLostTarget",105),
    AIS_SAFETY_RELATED_MESSAGE("AISSafetyRelatedMessage(usedtosilence)",106),
    AIS_CONNECTION_LOST("AISConnectionLost",107),
    NO_FIX("NoFix",108);

    private final int value;
    private final String description;

    SeatalkAlarm(String d, int v) {
        value = v;
        description = d;
    }

    public static SeatalkAlarm valueOf(int v) {
        switch (v) {
            case 0: return NO_ALARM;
            case 1: return SHALLOW_DEPTH;
            case 2: return DEEP_DEPTH;
            case 3: return SHALLOW_ANCHOR;
            case 4: return DEEP_ANCHOR;
            case 5: return OFF_COURSE;
            case 6: return AWA_HIGH;
            case 7: return AWA_LOW;
            case 8: return AWS_HIGH;
            case 9: return AWS_LOW;
            case 10: return TWA_HIGH;
            case 11: return TWA_LOW;
            case 12: return TWS_HIGH;
            case 13: return TWS_LOW;
            case 14: return WP_ARRIVAL;
            case 15: return BOAT_SPEED_HIGH;
            case 16: return BOAT_SPEED_LOW;
            case 17: return SEA_TEMP_HIGH;
            case 18: return SEA_TEMP_LOW;
            case 19: return PILOT_WATCH;
            case 20: return PILOT_OFF_COURSE;
            case 21: return PILOT_WIND_SHIFT;
            case 22: return PILOT_LOW_BATTERY;
            case 23: return PILOT_LAST_MINUTE_OF_WATCH;
            case 24: return PILOT_NO_NMEA_DATA;
            case 25: return PILOT_LARGE_XTE;
            case 26: return PILOT_NMEA_DATAERROR;
            case 27: return PILOT_CU_DISCONNECTED;
            case 28: return PILOT_AUTO_RELEASE;
            case 29: return PILOT_WAY_POINT_ADVANCE;
            case 30: return PILOT_DRIVE_STOPPED;
            case 31: return PILOT_TYPE_UNSPECIFIED;
            case 32: return PILOT_CALIBRATION_REQUIRED;
            case 33: return PILOT_LAST_HEADING;
            case 34: return PILOT_NO_PILOT;
            case 35: return PILOT_ROUTE_COMPLETE;
            case 36: return PILOT_VARIABLE_TEXT;
            case 37: return GPS_FAILURE;
            case 38: return MOB;
            case 39: return SEATALK1_ANCHOR;
            case 40: return PILOT_SWAPPED_MOTOR_POWER;
            case 41: return PILOT_STANDBY_TOO_FAST_TO_FISH;
            case 42: return PILOT_NO_GPS_FIX;
            case 43: return PILOT_NO_GPS_COG;
            case 44: return PILOT_START_UP;
            case 45: return PILOT_TOO_SLOW;
            case 46: return PILOT_NO_COMPASS;
            case 47: return PILOT_RATE_GYRO_FAULT;
            case 48: return PILOT_CURRENT_LIMIT;
            case 49: return PILOT_WAY_POINT_ADVANCE_PORT;
            case 50: return PILOT_WAY_POINT_ADVANCE_STBD;
            case 51: return PILOT_NO_WIND_DATA;
            case 52: return PILOT_NO_SPEED_DATA;
            case 53: return PILOT_SEATALK_FAIL1;
            case 54: return PILOT_SEATALK_FAIL2;
            case 55: return PILOT_WARNING_TOO_FAST_TO_FISH;
            case 56: return PILOT_AUTO_DOCKSIDE_FAIL;
            case 57: return PILOT_TURN_TOO_FAST;
            case 58: return PILOT_NO_NAV_DATA;
            case 59: return PILOT_LOST_WAYPOINT_DATA;
            case 60: return PILOT_EEPROM_CORRUPT;
            case 61: return PILOT_RUDDER_FEEDBACK_FAIL;
            case 62: return PILOT_AUTOLEARN_FAIL1;
            case 63: return PILOT_AUTOLEARN_FAIL2;
            case 64: return PILOT_AUTOLEARN_FAIL3;
            case 65: return PILOT_AUTOLEARN_FAIL4;
            case 66: return PILOT_AUTOLEARN_FAIL5;
            case 67: return PILOT_AUTOLEARN_FAIL6;
            case 68: return PILOT_WARNING_CAL_REQUIRED;
            case 69: return PILOT_WARNING_OFFCOURSE;
            case 70: return PILOT_WARNING_XTE;
            case 71: return PILOT_WARNING_WIND_SHIFT;
            case 72: return PILOT_WARNING_DRIVE_SHORT;
            case 73: return PILOT_WARNING_CLUTCH_SHORT;
            case 74: return PILOT_WARNING_SOLENOID_SHORT;
            case 75: return PILOT_JOYSTICK_FAULT;
            case 76: return PILOT_NO_JOYSTICK_DATA;
            case 77: return NOT_ASSIGNED1;
            case 78: return NOT_ASSIGNED2;
            case 79: return NOT_ASSIGNED3;
            case 80: return PILOT_INVALID_COMMAND;
            case 81: return AIS_TX_MALFUNCTION;
            case 82: return AIS_ANTENNA_VSWR_FAULT;
            case 83: return AIS_RX_CHANNEL_1_MALFUNCTION;
            case 84: return AIS_RX_CHANNEL_2_MALFUNCTION;
            case 85: return AIS_NO_SENSOR_POSITION_IN_USE;
            case 86: return AIS_NO_VALID_SOG_INFORMATION;
            case 87: return AIS_NO_VALID_COG_INFORMATION;
            case 88: return AIS_12V_ALARM;
            case 89: return AIS_6V_ALARM;
            case 90: return AIS_NOISE_THRESHOLD_EXCEEDED_CHANNEL_A;
            case 91: return AIS_NOISE_THRESHOLD_EXCEEDED_CHANNEL_B;
            case 92: return AIS_TRANSMITTER_PA_FAULT;
            case 93: return AIS_3V3_ALARM;
            case 94: return AIS_RX_CHANNEL_70_MALFUNCTION;
            case 95: return AIS_HEADING_LOST_INVALID;
            case 96: return AIS_INTERNAL_GPS_LOST;
            case 97: return AIS_NO_SENSOR_POSITION;
            case 98: return AIS_LOCK_FAILURE;
            case 99: return AIS_INTERNAL_GGA_TIMEOUT;
            case 100: return AIS_PROTOCOL_STACK_RESTART;
            case 101: return PILOT_NO_IPS_COMMUNICATIONS;
            case 102: return PILOT_POWER_ON_OR_SLEEP_SWITCH_RESET_WHILE_ENGAGED;
            case 103: return PILOT_UNEXPECTED_RESET_WHILE_ENGAGED;
            case 104: return AIS_DANGEROUS_TARGET;
            case 105: return AIS_LOST_TARGET;
            case 106: return AIS_SAFETY_RELATED_MESSAGE;
            case 107: return AIS_CONNECTION_LOST;
            case 108: return NO_FIX;
            default: return null;
        }
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return description;
    }
}
