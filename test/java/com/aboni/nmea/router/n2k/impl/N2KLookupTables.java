package com.aboni.nmea.router.n2k.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class N2KLookupTables {

    private N2KLookupTables() {
    }

    private static void load(Map<Integer, String> map, String sMap) {
        StringTokenizer tok = new StringTokenizer(sMap, ",");
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            try {
                int i = s.indexOf("=");
                if (i > 0) {
                    int value = Integer.parseInt(s.substring(0, i));
                    String sValue = s.substring(i + 1);
                    map.put(value, sValue);
                }
            } catch (Exception e) {
            }
        }
    }

    public static final Map<Integer, String> LOOKUP_INDUSTRY_CODE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_SHIP_TYPE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DEVICE_CLASS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_REPEAT_INDICATOR = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_AIS_TRANSCEIVER = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_AIS_ASSIGNED_MODE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ATON_TYPE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_AIS_SPECIAL_MANEUVER = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_POSITION_FIX_DEVICE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENGINE_INSTANCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENGINE_STATUS_1 = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENGINE_STATUS_2 = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_GEAR_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_POSITION_ACCURACY = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_RAIM_FLAG = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_RESPONSE_COMMAND = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_LANGUAGE_ID = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_STATE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_THRESHOLD_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_CATEGORY = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_TRIGGER_CONDITION = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ALERT_TYPE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_EQ = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_FILTER = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_POWER_FACTOR = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_TIME_STAMP = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_GNS_AIS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_RESIDUAL_MODE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_GNS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_GNS_METHOD = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_GNS_INTEGRITY = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_WIND_REFERENCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_WATER_REFERENCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_SYSTEM_TIME = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_MAGNETIC_VARIATION = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_YES_NO = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_OK_WARNING = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DIRECTION_REFERENCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_NAV_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_CHANNEL = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_GROUP = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_TYPE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_LIKE_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_SHUFFLE_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_REPEAT_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_PLAY_STATUS = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_SEATALK_ALARM_GROUP = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_ZONE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_ENTERTAINMENT_SOURCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_SEATALK_ALARM_ID = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_TEMPERATURE_SOURCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_HUMIDITY_SOURCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_PRESSURE_SOURCE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_FORMAT = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_CATEGORY = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_NATURE = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_FIRST_TELECOMMAND = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_SECOND_TELECOMMAND = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_DSC_EXPANSION_DATA = new HashMap<>();
    public static final Map<Integer, String> LOOKUP_SEATALK_ALARM_STATUS = new HashMap<>();

    static {
        load(LOOKUP_INDUSTRY_CODE, "0=Global,1=Highway,2=Agriculture,3=Construction,4=Marine,5=Industrial");

        load(LOOKUP_SHIP_TYPE,
                "0=unavailable" +
                        ",20=Wing In Ground,29=Wing In Ground (no other information)" +
                        ",30=Fishing,31=Towing,32=Towing exceeds 200m or wider than 25m,33=Engaged in dredging or underwater operations,34=Engaged in " +
                        "diving operations" +
                        ",35=Engaged in military operations,36=Sailing,37=Pleasure" +
                        ",40=High speed craft,41=High speed craft carrying dangerous goods,42=High speed craft hazard cat B,43=High speed craft " +
                        "hazard cat C,44=High speed craft hazard cat D,49=High speed craft (no additional information)" +
                        ",50=Pilot vessel,51=SAR,52=Tug,53=Port tender,54=Anti-pollution,55=Law enforcement,56=Spare,57=Spare #2,58=Medical,59=RR " +
                        "Resolution No.18" +
                        ",60=Passenger ship,69=Passenger ship (no additional information)" +
                        ",70=Cargo ship,71=Cargo ship carrying dangerous goods,72=Cargo ship hazard cat B,73=Cargo ship hazard cat C,74=Cargo ship " +
                        "hazard cat D,79=Cargo ship (no additional information)" +
                        ",80=Tanker,81=Tanker carrying dangerous goods,82=Tanker hazard cat B,83=Tanker hazard cat C,84=Tanker hazard cat D,89=Tanker " +
                        "(no additional information)" +
                        ",90=Other,91=Other carrying dangerous goods,92=Other hazard cat B,93=Other hazard cat C,94=Other hazard cat D,99=Other (no " +
                        "additional information)");

        /* http://www.nmea.org/Assets/20120726%20nmea%202000%20class%20&%20function%20codes%20v%202.00.pdf */
        load(LOOKUP_DEVICE_CLASS
                , "0=Reserved for 2000 Use" +
                        ",10=System tools" +
                        ",20=Safety systems" +
                        ",25=Internetwork device" +
                        ",30=Electrical Distribution" +
                        ",35=Electrical Generation" +
                        ",40=Steering and Control surfaces" +
                        ",50=Propulsion" +
                        ",60=Navigation" +
                        ",70=Communication" +
                        ",75=Sensor Communication Interface" +
                        ",80=Instrumentation/general systems" +
                        ",85=External Environment" +
                        ",90=Internal Environment" +
                        ",100=Deck + cargo + fishing equipment systems" +
                        ",120=Display" +
                        ",125=Entertainment");

        load(LOOKUP_REPEAT_INDICATOR
                , "0=Initial,1=First retransmission,2=Second retransmission,3=Final retransmission");

        load(LOOKUP_AIS_TRANSCEIVER
                , "0=Channel A VDL reception" +
                        ",1=Channel B VDL reception" +
                        ",2=Channel A VDL transmission" +
                        ",3=Channel B VDL transmission" +
                        ",4=Own information not broadcast" +
                        ",5=Reserved");

        load(LOOKUP_AIS_ASSIGNED_MODE
                , "0=Autonomous and continuous" +
                        ",1=Assigned mode");

        load(LOOKUP_ATON_TYPE
                , "0=Default: Type of AtoN not specified" +
                        ",1=Referece point" +
                        ",2=RACON" +
                        ",3=Fixed structure off-shore" +
                        ",4=Reserved for future use" +
                        ",5=Fixed light: without sectors" +
                        ",6=Fixed light: with sectors" +
                        ",7=Fixed leading light front" +
                        ",8=Fixed leading light rear" +
                        ",9=Fixed beacon: cardinal N" +
                        ",10=Fixed beacon: cardinal E" +
                        ",11=Fixed beacon: cardinal S" +
                        ",12=Fixed beacon: cardinal W" +
                        ",13=Fixed beacon: port hand" +
                        ",14=Fixed beacon: starboard hand" +
                        ",15=Fixed beacon: preferred channel port hand" +
                        ",16=Fixed beacon: preferred channel starboard hand" +
                        ",17=Fixed beacon: isolated danger" +
                        ",18=Fixed beacon: safe water" +
                        ",19=Fixed beacon: special mark" +
                        ",20=Floating AtoN: cardinal N" +
                        ",21=Floating AtoN: cardinal E" +
                        ",22=Floating AtoN: cardinal S" +
                        ",23=Floating AtoN: cardinal W" +
                        ",24=Floating AtoN: port hand mark" +
                        ",25=Floating AtoN: starboard hand mark" +
                        ",26=Floating AtoN: preferred channel port hand" +
                        ",27=Floating AtoN: preferred channel starboard hand" +
                        ",28=Floating AtoN: isolated danger" +
                        ",29=Floating AtoN: safe water" +
                        ",30=Floating AtoN: special mark" +
                        ",31=Floating AtoN: light vessel/LANBY/rigs");

        load(LOOKUP_AIS_SPECIAL_MANEUVER
                , "0=Not available" +
                        ",1=Not engaged in special maneuver" +
                        ",2=Engaged in special maneuver" +
                        ",3=Reserverd");

        load(LOOKUP_POSITION_FIX_DEVICE
                , "0=Default: undefined" +
                        ",1=GPS" +
                        ",2=GLONASS" +
                        ",3=Combined GPS/GLONASS" +
                        ",4=Loran-C" +
                        ",5=Chayka" +
                        ",6=Integrated navigation system" +
                        ",7=Surveyed" +
                        ",8=Galileo" +
                        ",15=Internal GNSS");

        load(LOOKUP_ENGINE_INSTANCE, "0=Single Engine or Dual Engine Port,1=Dual Engine Starboard");

// http://www.osukl.com/wp-content/uploads/2015/04/3155-UM.pdf
        load(LOOKUP_ENGINE_STATUS_1
                , "0=Check Engine,1=Over Temperature,2=Low Oil Pressure,3=Low Oil Level,4=Low Fuel Pressure,5=Low System Voltage,6=Low Coolant " +
                        "Level,7=Water Flow,8=Water In Fuel,9=Charge Indicator,10=Preheat Indicator,11=High Boost Pressure,12=Rev Limit " +
                        "Exceeded,13=EGR System,14=Throttle Position Sensor,15=Emergency Stop");
        load(LOOKUP_ENGINE_STATUS_2
                , "0=Warning Level 1,1=Warning Level 2,2=Power Reduction,3=Maintenance Needed,4=Engine Comm Error,5=Sub or Secondary " +
                        "Throttle,6=Neutral Start Protect,7=Engine Shutting Down");

        load(LOOKUP_GEAR_STATUS, "0=Forward,1=Neutral,2=Reverse");

        load(LOOKUP_POSITION_ACCURACY, "0=Low,1=High");

        load(LOOKUP_RAIM_FLAG, "0=not in use,1=in use");

        load(LOOKUP_TIME_STAMP
                , "60=Not available,61=Manual input mode,62=Dead reckoning mode,63=Positioning system is inoperative");

        load(LOOKUP_GNS_AIS
                , "0=undefined,1=GPS,2=GLONASS,3=GPS+GLONASS,4=Loran-C,5=Chayka,6=integrated,7=surveyed,8=Galileo");
        load(LOOKUP_GNS
                , "0=GPS,1=GLONASS,2=GPS+GLONASS,3=GPS+SBAS/WAAS,4=GPS+SBAS/WAAS+GLONASS,5=Chayka,6=integrated,7=surveyed,8=Galileo");

        load(LOOKUP_GNS_METHOD
                , "0=no GNSS,1=GNSS fix,2=DGNSS fix,3=Precise GNSS,4=RTK Fixed Integer,5=RTK float,6=Estimated (DR) mode,7=Manual " +
                        "Input,8=Simulate mode");

        load(LOOKUP_GNS_INTEGRITY, "0=No integrity checking,1=Safe,2=Caution");

        load(LOOKUP_SYSTEM_TIME
                , "0=GPS,1=GLONASS,2=Radio Station,3=Local Cesium clock,4=Local Rubidium clock,5=Local Crystal clock");

        load(LOOKUP_MAGNETIC_VARIATION
                , "0=Manual" +
                        ",1=Automatic Chart" +
                        ",2=Automatic Table" +
                        ",3=Automatic Calculation" +
                        ",4=WMM 2000" +
                        ",5=WMM 2005" +
                        ",6=WMM 2010" +
                        ",7=WMM 2015" +
                        ",8=WMM 2020");

        load(LOOKUP_RESIDUAL_MODE, "0=Autonomous,1=Differential enhanced,2=Estimated,3=Simulator,4=Manual");

        load(LOOKUP_WIND_REFERENCE
                , "0=True (ground referenced to North),1=Magnetic (ground referenced to Magnetic North),2=Apparent,3=True (boat " +
                        "referenced),4=True (water referenced)");

        load(LOOKUP_WATER_REFERENCE
                , "0=Paddle wheel,1=Pitot tube,2=Doppler,3=Correlation (ultra sound),4=Electro Magnetic");

        load(LOOKUP_YES_NO, "0=No,1=Yes,10=Error,11=Unavailable");
        load(LOOKUP_OK_WARNING, "0=OK,1=Warning");

        load(LOOKUP_DIRECTION_REFERENCE, "0=True,1=Magnetic,2=Error,3=Null");

        load(LOOKUP_NAV_STATUS
                , "0=Under way using engine" +
                        ",1=At anchor" +
                        ",2=Not under command" +
                        ",3=Restricted manoeuverability" +
                        ",4=Constrained by her draught" +
                        ",5=Moored" +
                        ",6=Aground" +
                        ",7=Engaged in Fishing" +
                        ",8=Under way sailing" +
                        ",9=Hazardous material - High Speed" +
                        ",10=Hazardous material - Wing in Ground" +
                        ",14=AIS-SART");

        load(LOOKUP_POWER_FACTOR, "0=Leading,1=Lagging,2=Error");

        load(LOOKUP_TEMPERATURE_SOURCE
                , "0=Sea Temperature" +
                        ",1=Outside Temperature" +
                        ",2=Inside Temperature" +
                        ",3=Engine Room Temperature" +
                        ",4=Main Cabin Temperature" +
                        ",5=Live Well Temperature" +
                        ",6=Bait Well Temperature" +
                        ",7=Refridgeration Temperature" +
                        ",8=Heating System Temperature" +
                        ",9=Dew Point Temperature" +
                        ",10=Apparent Wind Chill Temperature" +
                        ",11=Theoretical Wind Chill Temperature" +
                        ",12=Heat Index Temperature" +
                        ",13=Freezer Temperature" +
                        ",14=Exhaust Gas Temperature");

        load(LOOKUP_HUMIDITY_SOURCE
                , "0=Inside" +
                        ",1=Outside");

        load(LOOKUP_PRESSURE_SOURCE
                , "0=Atmospheric" +
                        ",1=Water" +
                        ",2=Steam" +
                        ",3=Compressed Air" +
                        ",4=Hydraulic");

        load(LOOKUP_DSC_FORMAT
                , "102=Geographical area" +
                        ",112=Distress" +
                        ",114=Common interest" +
                        ",116=All ships" +
                        ",120=Individual stations" +
                        ",121=Non-calling purpose" +
                        ",123=Individual station automatic");

        load(LOOKUP_DSC_CATEGORY
                , "100=Routine" +
                        ",108=Safety" +
                        ",110=Urgency" +
                        ",112=Distress");

        load(LOOKUP_DSC_NATURE
                , "100=Fire" +
                        ",101=Flooding" +
                        ",102=Collision" +
                        ",103=Grounding" +
                        ",104=Listing" +
                        ",105=Sinking" +
                        ",106=Disabled and adrift" +
                        ",107=Undesignated" +
                        ",108=Abandoning ship" +
                        ",109=Piracy" +
                        ",110=Man overboard" +
                        ",112=EPIRB emission");

        load(LOOKUP_DSC_FIRST_TELECOMMAND
                , "100=F3E/G3E All modes TP" +
                        ",101=F3E/G3E duplex TP" +
                        ",103=Polling" +
                        ",104=Unable to comply" +
                        ",105=End of call" +
                        ",106=Data" +
                        ",109=J3E TP" +
                        ",110=Distress acknowledgement" +
                        ",112=Distress relay" +
                        ",113=F1B/J2B TTY-FEC" +
                        ",115=F1B/J2B TTY-ARQ" +
                        ",118=Test" +
                        ",121=Ship position or location registration updating" +
                        ",126=No information");

        load(LOOKUP_DSC_SECOND_TELECOMMAND
                , "100=No reason given" +
                        ",101=Congestion at MSC" +
                        ",102=Busy" +
                        ",103=Queue indication" +
                        ",104=Station barred" +
                        ",105=No operator available" +
                        ",106=Operator temporarily unavailable" +
                        ",107=Equipment disabled" +
                        ",108=Unable to use proposed channel" +
                        ",109=Unable to use proposed mode" +
                        ",110=Ships and aircraft of States not parties to an armed conflict" +
                        ",111=Medical transports" +
                        ",112=Pay phone/public call office" +
                        ",113=Fax/data" +
                        ",126=No information");

        load(LOOKUP_DSC_EXPANSION_DATA
                , "100=Enhanced position" +
                        ",101=Source and datum of position" +
                        ",102=SOG" +
                        ",103=COG" +
                        ",104=Additional station identification" +
                        ",105=Enhanced geographic area" +
                        ",106=Number of persons on board");

        load(LOOKUP_SEATALK_ALARM_STATUS
                , "0=Alarm condition not met" +
                        ",1=Alarm condition met and not silenced" +
                        ",2=Alarm condition met and silenced");

        load(LOOKUP_SEATALK_ALARM_ID
                , "0=No Alarm" +
                        ",1=Shallow Depth" +
                        ",2=Deep Depth" +
                        ",3=Shallow Anchor" +
                        ",4=Deep Anchor" +
                        ",5=Off Course" +
                        ",6=AWA High" +
                        ",7=AWA Low" +
                        ",8=AWS High" +
                        ",9=AWS Low" +
                        ",10=TWA High" +
                        ",11=TWA Low" +
                        ",12=TWS High" +
                        ",13=TWS Low" +
                        ",14=WP Arrival" +
                        ",15=Boat Speed High" +
                        ",16=Boat Speed Low" +
                        ",17=Sea Temp High" +
                        ",18=Sea Temp Low" +
                        ",19=Pilot Watch" +
                        ",20=Pilot Off Course" +
                        ",21=Pilot Wind Shift" +
                        ",22=Pilot Low Battery" +
                        ",23=Pilot Last Minute Of Watch" +
                        ",24=Pilot No NMEA Data" +
                        ",25=Pilot Large XTE" +
                        ",26=Pilot NMEA DataError" +
                        ",27=Pilot CU Disconnected" +
                        ",28=Pilot Auto Release" +
                        ",29=Pilot Way Point Advance" +
                        ",30=Pilot Drive Stopped" +
                        ",31=Pilot Type Unspecified" +
                        ",32=Pilot Calibration Required" +
                        ",33=Pilot Last Heading" +
                        ",34=Pilot No Pilot" +
                        ",35=Pilot Route Complete" +
                        ",36=Pilot Variable Text" +
                        ",37=GPS Failure" +
                        ",38=MOB" +
                        ",39=Seatalk1 Anchor" +
                        ",40=Pilot Swapped Motor Power" +
                        ",41=Pilot Standby Too Fast To Fish" +
                        ",42=Pilot No GPS Fix" +
                        ",43=Pilot No GPS COG" +
                        ",44=Pilot Start Up" +
                        ",45=Pilot Too Slow" +
                        ",46=Pilot No Compass" +
                        ",47=Pilot Rate Gyro Fault" +
                        ",48=Pilot Current Limit" +
                        ",49=Pilot Way Point Advance Port" +
                        ",50=Pilot Way Point Advance Stbd" +
                        ",51=Pilot No Wind Data" +
                        ",52=Pilot No Speed Data" +
                        ",53=Pilot Seatalk Fail1" +
                        ",54=Pilot Seatalk Fail2" +
                        ",55=Pilot Warning Too Fast To Fish" +
                        ",56=Pilot Auto Dockside Fail" +
                        ",57=Pilot Turn Too Fast" +
                        ",58=Pilot No Nav Data" +
                        ",59=Pilot Lost Waypoint Data" +
                        ",60=Pilot EEPROM Corrupt" +
                        ",61=Pilot Rudder Feedback Fail" +
                        ",62=Pilot Autolearn Fail1" +
                        ",63=Pilot Autolearn Fail2" +
                        ",64=Pilot Autolearn Fail3" +
                        ",65=Pilot Autolearn Fail4" +
                        ",66=Pilot Autolearn Fail5" +
                        ",67=Pilot Autolearn Fail6" +
                        ",68=Pilot Warning Cal Required" +
                        ",69=Pilot Warning OffCourse" +
                        ",70=Pilot Warning XTE" +
                        ",71=Pilot Warning Wind Shift" +
                        ",72=Pilot Warning Drive Short" +
                        ",73=Pilot Warning Clutch Short" +
                        ",74=Pilot Warning Solenoid Short" +
                        ",75=Pilot Joystick Fault" +
                        ",76=Pilot No Joystick Data" +
                        ",77=not assigned" +
                        ",78=not assigned" +
                        ",79=not assigned" +
                        ",80=Pilot Invalid Command" +
                        ",81=AIS TX Malfunction" +
                        ",82=AIS Antenna VSWR fault" +
                        ",83=AIS Rx channel 1 malfunction" +
                        ",84=AIS Rx channel 2 malfunction" +
                        ",85=AIS No sensor position in use" +
                        ",86=AIS No valid SOG information" +
                        ",87=AIS No valid COG information" +
                        ",88=AIS 12V alarm" +
                        ",89=AIS 6V alarm" +
                        ",90=AIS Noise threshold exceeded channel A" +
                        ",91=AIS Noise threshold exceeded channel B" +
                        ",92=AIS Transmitter PA fault" +
                        ",93=AIS 3V3 alarm" +
                        ",94=AIS Rx channel 70 malfunction" +
                        ",95=AIS Heading lost/invalid" +
                        ",96=AIS internal GPS lost" +
                        ",97=AIS No sensor position" +
                        ",98=AIS Lock failure" +
                        ",99=AIS Internal GGA timeout" +
                        ",100=AIS Protocol stack restart" +
                        ",101=Pilot No IPS communications" +
                        ",102=Pilot Power-On or Sleep-Switch Reset While Engaged     " +
                        ",103=Pilot Unexpected Reset While Engaged" +
                        ",104=AIS Dangerous Target" +
                        ",105=AIS Lost Target" +
                        ",106=AIS Safety Related Message (used to silence)" +
                        ",107=AIS Connection Lost" +
                        ",108=No Fix");

        load(LOOKUP_SEATALK_ALARM_GROUP
                , "0=Instrument" +
                        ",1=Autopilot" +
                        ",2=Radar" +
                        ",3=Chart Plotter" +
                        ",4=AIS");

        load(LOOKUP_ENTERTAINMENT_ZONE
                , "0=All zones" +
                        ",1=Zone 1" +
                        ",2=Zone 2" +
                        ",3=Zone 3" +
                        ",4=Zone 4");

        load(LOOKUP_ENTERTAINMENT_SOURCE
                , "0=Vessel alarm" +
                        ",1=AM" +
                        ",2=FM" +
                        ",3=Weather" +
                        ",4=DAB" +
                        ",5=Aux" +
                        ",6=USB" +
                        ",7=CD" +
                        ",8=MP3" +
                        ",9=Apple iOS" +
                        ",10=Android" +
                        ",11=Bluetooth" +
                        ",12=Sirius XM" +
                        ",13=Pandora" +
                        ",14=Spotify" +
                        ",15=Slacker" +
                        ",16=Songza" +
                        ",17=Apple Radio" +
                        ",18=Last FM" +
                        ",19=Ethernet" +
                        ",20=Video MP4" +
                        ",21=Video DVD" +
                        ",22=Video BluRay" +
                        ",23=HDMI" +
                        ",24=Video");

        load(LOOKUP_ENTERTAINMENT_PLAY_STATUS
                , "0=Play" +
                        ",1=Pause" +
                        ",2=Stop" +
                        ",3=FF (1x)" +
                        ",4=FF (2x)" +
                        ",5=FF (3x)" +
                        ",6=FF (4x)" +
                        ",7=RW (1x)" +
                        ",8=RW (2x)" +
                        ",9=RW (3x)" +
                        ",10=RW (4x)" +
                        ",11=Skip ahead" +
                        ",12=Skip back" +
                        ",13=Jog ahead" +
                        ",14=Jog back" +
                        ",15=Seek up" +
                        ",16=Seek down" +
                        ",17=Scan up" +
                        ",18=Scan down" +
                        ",19=Tune up" +
                        ",20=Tune down" +
                        ",21=Slow motion (.75x)" +
                        ",22=Slow motion (.5x)" +
                        ",23=Slow motion (.25x)" +
                        ",24=Slow motion (.125x)" +
                        ",25=Source renaming");

        load(LOOKUP_ENTERTAINMENT_REPEAT_STATUS
                , "0=Off" +
                        ",1=One" +
                        ",2=All");

        load(LOOKUP_ENTERTAINMENT_SHUFFLE_STATUS
                , "0=Off" +
                        ",1=Play queue" +
                        ",2=All");

        load(LOOKUP_ENTERTAINMENT_LIKE_STATUS
                , "0=None" +
                        ",1=Thumbs up" +
                        ",2=Thumbs down");

        load(LOOKUP_ENTERTAINMENT_TYPE
                , "0=File" +
                        ",1=Playlist Name" +
                        ",2=Genre Name" +
                        ",3=Album Name" +
                        ",4=Artist Name" +
                        ",5=Track Name" +
                        ",6=Station Name" +
                        ",7=Station Number" +
                        ",8=Favourite Number" +
                        ",9=Play Queue" +
                        ",10=Content Info");

        load(LOOKUP_ENTERTAINMENT_GROUP
                , "0=File" +
                        ",1=Playlist Name" +
                        ",2=Genre Name" +
                        ",3=Album Name" +
                        ",4=Artist Name" +
                        ",5=Track Name" +
                        ",6=Station Name" +
                        ",7=Station Number" +
                        ",8=Favourite Number" +
                        ",9=Play Queue" +
                        ",10=Content Info");

        load(LOOKUP_ENTERTAINMENT_CHANNEL
                , "0=All channels" +
                        ",1=Stereo full range" +
                        ",2=Stereo front" +
                        ",3=Stereo back" +
                        ",4=Stereo surround" +
                        ",5=Center" +
                        ",6=Subwoofer" +
                        ",7=Front left" +
                        ",8=Front right" +
                        ",9=Back left" +
                        ",10=Back right" +
                        ",11=Surround left" +
                        ",12=Surround right");

        load(LOOKUP_ENTERTAINMENT_EQ
                , "0=Flat" +
                        ",1=Rock" +
                        ",2=Hall" +
                        ",3=Jazz" +
                        ",4=Pop" +
                        ",5=Live" +
                        ",6=Classic" +
                        ",7=Vocal" +
                        ",8=Arena" +
                        ",9=Cinema" +
                        ",10=Custom");

        load(LOOKUP_ENTERTAINMENT_FILTER
                , "0=Full range" +
                        ",1=High pass" +
                        ",2=Low pass" +
                        ",3=Band pass" +
                        ",4=Notch filter");

        load(LOOKUP_ALERT_TYPE
                , "0=Reserved" +
                        ",1=Emergency Alarm" +
                        ",2=Alarm" +
                        ",3=Reserved" +
                        ",4=Reserved" +
                        ",5=Warning" +
                        ",6=Reserved" +
                        ",7=Reserved" +
                        ",8=Caution" +
                        ",13=Reserved" +
                        ",14=Data out of range" +
                        ",15=Data not available");

        load(LOOKUP_ALERT_CATEGORY
                , "0=Navigational" +
                        ",1=Technical" +
                        ",13=Reserved" +
                        ",14=Data out of range" +
                        ",15=Data not available");

        load(LOOKUP_ALERT_TRIGGER_CONDITION
                , "0=Manual" +
                        ",1=Auto" +
                        ",2=Test" +
                        ",3=Disabled" +
                        ",13=Reserved" +
                        ",14=Data out of range" +
                        ",15=Data not available");

        load(LOOKUP_ALERT_THRESHOLD_STATUS
                , "0=Normal" +
                        ",1=Threshold Exceeded" +
                        ",2=Extreme Threshold Exceeded" +
                        ",3=Low Threshold Exceeded" +
                        ",4=Acknowledged" +
                        ",5=Awaiting Acknowledge" +
                        ",253=Reserved" +
                        ",254=Data out of range" +
                        ",255=Data not available");

        load(LOOKUP_ALERT_STATE
                , "0=Disabled" +
                        ",1=Normal" +
                        ",2=Active" +
                        ",3=Silenced" +
                        ",4=Acknowledged" +
                        ",5=Awaiting Acknowledge" +
                        ",253=Reserved" +
                        ",254=Data out of range" +
                        ",255=Data not available");

        load(LOOKUP_ALERT_LANGUAGE_ID
                , "0=English (US)" +
                        ",1=English (UK)" +
                        ",2=Arabic" +
                        ",3=Chinese (simplified)" +
                        ",4=Croatian" +
                        ",5=Danish" +
                        ",6=Dutch" +
                        ",7=Finnish" +
                        ",8=French" +
                        ",9=German" +
                        ",10=Greek" +
                        ",11=Italian" +
                        ",12=Japanese" +
                        ",13=Korean" +
                        ",14=Norwegian" +
                        ",15=Polish" +
                        ",16=Portuguese" +
                        ",17=Russian" +
                        ",18=Spanish" +
                        ",19=Sweedish");

        load(LOOKUP_ALERT_RESPONSE_COMMAND
                , "0=Acknowledge" +
                        ",1=Temporary Silence" +
                        ",2=Test Command off" +
                        ",3=Test Command on");
    }
}