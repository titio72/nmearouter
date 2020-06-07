package com.aboni.nmea.router.processors;

import com.aboni.misc.Utils;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;
import org.json.JSONObject;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class CANBOATDecoder {


    @Inject
    public CANBOATDecoder() {

    }
    /*
    {"timestamp":"2020-06-05-18:03:25.626","prio":3,"src":105,"dst":255,"pgn":128267,"description":"Water Depth","fields":{"SID":0,"Depth":3.42,"Offset":0.200}}
    {"timestamp":"2020-06-05-18:03:25.626","prio":2,"src":105,"dst":255,"pgn":130306,"description":"Wind Data","fields":{"SID":0,"Wind Speed":4.01,"Wind Angle":347.4,"Reference":"Apparent"}}
    {"timestamp":"2020-06-05-18:03:25.627","prio":2,"src":105,"dst":255,"pgn":128259,"description":"Speed","fields":{"SID":0,"Speed Water Referenced":0.00,"Speed Water Referenced Type":"Paddle wheel"}}
    {"timestamp":"2020-06-05-18:03:25.630","prio":5,"src":105,"dst":255,"pgn":130310,"description":"Environmental Parameters","fields":{"SID":0,"Water Temperature":20.80}}
    {"timestamp":"2020-06-05-18:03:25.680","prio":2,"src":204,"dst":255,"pgn":127250,"description":"Vessel Heading","fields":{"Heading":279.5,"Reference":"Magnetic"}}
    {"timestamp":"2020-06-05-18:03:25.683","prio":2,"src":204,"dst":255,"pgn":127251,"description":"Rate of Turn","fields":{"Rate":-0.10792}}
    {"timestamp":"2020-06-05-18:03:25.684","prio":2,"src":204,"dst":255,"pgn":127257,"description":"Attitude","fields":{"Yaw":-80.5,"Pitch":2.5,"Roll":-4.8}}
    {"timestamp":"2020-06-05-20:36:53.057","prio":2,"src":2,"dst":255,"pgn":129026,"description":"COG & SOG, Rapid Update","fields":{"COG Reference":"True","COG":90.0,"SOG":0.09}}
    {"timestamp":"2020-06-05-20:36:32.435","prio":2,"src":2,"dst":255,"pgn":129025,"description":"Position, Rapid Update","fields":{"Latitude":43.6774763,"Longitude":10.2739919}}
    {"timestamp":"2020-06-05-20:50:55.400","prio":3,"src":2,"dst":255,"pgn":126992,"description":"System Time","fields":{"Date":"2020.06.06", "Time": "10:07:39.07940"}}
    {"timestamp":"2020-06-05-23:56:39.582","prio":2,"src":204,"dst":255,"pgn":127245,"description":"Rudder","fields":{"Instance":0,"Position":4.7}}

    */

    /*
    {"timestamp":"2020-06-05-21:00:17.423","prio":4,"src":0,"dst":255,"pgn":129038,"description":"AIS Class A Position Report",
    "fields":{
    "Message ID":1,
    "User ID":247344100,
    "Longitude":10.3025484,"Latitude":43.5613784,
    "Position Accuracy":"Low",
    "RAIM":"not in use",
    "Time Stamp":"56",
    "COG":93.1,
    "SOG":0.00,
    "Communication State":"0",
    "AIS Transceiver information":"Channel A VDL reception",
    "Heading":287.0,
    "Rate of Turn":0.00,
    "Nav Status":"Restricted manoeuverability",
    "AIS Spare":"6"}}


    {"timestamp":"2020-06-05-21:00:17.726","prio":4,"src":0,"dst":255,"pgn":129041,"description":"AIS Aids to Navigation (AtoN) Report",
    "fields":{
    "Message ID":21,
    "Repeat Indicator":"First retransmission",
    "User ID":992471020,"Longitude":10.0368747,
    "Latitude":44.0362586,
    "Position Accuracy":"Low",
    "AIS RAIM Flag":"not in use",
    "Length/Diameter":2.0,
    "Beam/Diameter":2.0,
    "Position Reference from Starboard Edge":1.0,
    "AtoN Type":"Fixed light: without sectors",
    "Off Position Indicator":"No",
    "Virtual AtoN Flag":"No",
    "Assigned Mode Flag":"Autonomous and continuous",
    "AIS Spare":"0",
    "Position Fixing Device Type":"Default: undefined",
    "AIS Transceiver information":"Channel B VDL reception",
    "AtoN Name":"E1328 MARINA DI CARR   "}}



    {"timestamp":"2020-06-05-21:00:17.935","prio":4,"src":0,"dst":255,"pgn":129041,"description":"AIS Aids to Navigation (AtoN) Report",
    "fields":{
    "Message ID":21,
    "Repeat Indicator":"First retransmission",
    "User ID":992471020,
    "Longitude":10.0368747,
    "Latitude":44.0362586,
    "Position Accuracy":"Low",
    "AIS RAIM Flag":"not in use",
    "Length/Diameter":2.0,
    "Beam/Diameter":2.0,
    "Position Reference from Starboard Edge":1.0,
    "AtoN Type":"Fixed light: without sectors",
    "Off Position Indicator":"No",
    "Virtual AtoN Flag":"No",
    "Assigned Mode Flag":"Autonomous and continuous",
    "AIS Spare":"0",
    "Position Fixing Device Type":"Default: undefined",
    "AIS Transceiver information":"Channel A VDL reception",
    "AtoN Name":"E1328 MARINA DI CARR   "}}

    {"timestamp":"2020-06-05-21:00:18.114","prio":7,"src":0,"dst":255,"pgn":129793,"description":"AIS UTC and Date Report",
    "fields":{
    "Message ID":4,"Repeat Indicator":"Initial",
    "User ID":2470054,
    "Longitude": 9.8166666,
    "Latitude":44.0666656,
    "Position Accuracy":"Low",
    "RAIM":"not in use",
    "Position Time": "10:17:00",
    "Communication State":"0",
    "AIS Transceiver information":"Channel B VDL reception",
    "Position Date":"2020.06.06",
    "GNSS type":"surveyed",
    "Spare":"0"}}
     */


    /*
    {"timestamp":"2020-06-05-20:46:35.360","prio":6,"src":2,"dst":255,"pgn":129540,"description":"GNSS Sats in View",
    "fields":{"SID":58,"Sats in View":15,"list":[
    {"PRN":2,"Elevation":13.0,"Azimuth":39.0,"SNR":26.00,"Range residuals":0,"Status":"Used"},
    {"PRN":4,"Elevation":1.0,"Azimuth":336.0,"SNR":20.00,"Range residuals":0,"Status":"Used"},
    {"PRN":5,"Elevation":11.0,"Azimuth":75.0,"SNR":34.00,"Range residuals":0,"Status":"Used"},
    {"PRN":12,"Elevation":20.0,"Azimuth":104.0,"SNR":35.00,"Range residuals":0,"Status":"Used"},
    {"PRN":14,"Elevation":11.0,"Azimuth":244.0,"SNR":21.00,"Range residuals":0,"Status":"Used"},
    {"PRN":18,"Elevation":35.0,"Azimuth":176.0,"SNR":26.00,"Range residuals":0,"Status":"Used"},
    {"PRN":21,"Elevation":20.0,"Azimuth":193.0,"SNR":18.00,"Range residuals":0,"Status":"Used"},
    {"PRN":25,"Elevation":50.0,"Azimuth":101.0,"SNR":38.00,"Range residuals":0,"Status":"Used"},
    {"PRN":26,"Elevation":27.0,"Azimuth":299.0,"SNR":31.00,"Range residuals":0,"Status":"Used"},
    {"PRN":29,"Elevation":75.0,"Azimuth":27.0,"SNR":36.00,"Range residuals":0,"Status":"Used"},
    {"PRN":31,"Elevation":56.0,"Azimuth":278.0,"SNR":35.00,"Range residuals":0,"Status":"Used"},
    {"PRN":33,"Elevation":33.0,"Azimuth":214.0,"SNR":0.00,"Range residuals":0,"Status":"Not tracked"},
    {"PRN":37,"Elevation":38.0,"Azimuth":163.0,"SNR":0.00,"Range residuals":0,"Status":"Not tracked"},
    {"PRN":39,"Elevation":38.0,"Azimuth":159.0,"SNR":0.00,"Range residuals":0,"Status":"Not tracked"},
    {"PRN":32,"Elevation":1.0,"Azimuth":225.0,"SNR":0.00,"Range residuals":0,"Status":"Not tracked"}]}}
     */

    private JSONObject lastHeading = null;
    private JSONObject lastSOGCOG = null;
    private Instant lastTime;
    private long lastLocalTime;

    public Sentence getSentence(JSONObject canboatSentence) {
        int pgn = canboatSentence.getInt("pgn");
        JSONObject fields = canboatSentence.getJSONObject("fields");
        switch (pgn) {
            case 130306:
                return handleWind(fields);
            case 128267:
                return handleDepth(fields);
            case 128259:
                return handleSpeed(fields);
            case 130310:
                return handleWaterTemp(fields);
            case 127250:
                return handleHeading(fields);
            case 129026:
                return handleSOGCOG(fields);
            case 129025:
                return handlePosition(fields);
            case 126992:
                return handeSystemTime(fields);
            case 127245:
                return handleRudder(fields);
            case 127257:
                return handleAttitude(fields);
            default:
                return null;
        }
    }

    private Sentence handleAttitude(JSONObject fields) {
        // {"timestamp":"2020-06-05-18:03:25.684","prio":2,"src":204,"dst":255,"pgn":127257,"description":"Attitude","fields":{"Yaw":-80.5,"Pitch":2.5,"Roll":-4.8}}
        double roll = fields.getDouble("Roll");
        double pitch = fields.getDouble("Pitch");
        double yaw = fields.getDouble("Yaw");
        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR);
        xdr.addMeasurement(new Measurement("A", yaw, "D", "YAW"));
        xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
        xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
        return xdr;
    }

    private Sentence handleRudder(JSONObject fields) {
        // {"timestamp":"2020-06-05-23:56:39.582","prio":2,"src":204,"dst":255,"pgn":127245,"description":"Rudder","fields":{"Instance":0,"Position":4.7}}
        if (fields.getInt("Instance")==0) {
            RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
            double angle = fields.getDouble("Position");
            rsa.setRudderAngle(Side.STARBOARD, Utils.normalizeDegrees180To180(angle));
            return rsa;
        } else {
            return null;
        }

    }

    private Sentence handeSystemTime(JSONObject fields) {
        // {"timestamp":"2020-06-05-20:50:55.400","prio":3,"src":2,"dst":255,"pgn":126992,"description":"System Time","fields":{"Date":"2020.06.06", "Time": "10:07:39.07940"}}
        String sDate = fields.getString("Date").replace(".", "-");
        String sTime = fields.getString("Time");
        lastTime = Instant.parse(sDate + "T" + sTime + "Z");
        lastLocalTime = System.currentTimeMillis();
        return null;
    }

    private Sentence handlePosition(JSONObject fields) {
        //{"timestamp":"2020-06-05-20:36:32.435","prio":2,"src":2,"dst":255,"pgn":129025,"description":"Position, Rapid Update","fields":{"Latitude":43.6774763,"Longitude":10.2739919}}
        RMCSentence rmc = (RMCSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RMC);
        rmc.setPosition(new Position(fields.getDouble("Latitude"), fields.getDouble("Longitude")));
        rmc.setVariation(0.0);
        rmc.setDirectionOfVariation(CompassPoint.EAST);
        rmc.setMode(FaaMode.AUTOMATIC);
        rmc.setStatus(DataStatus.ACTIVE);
        if (lastSOGCOG!=null) {
            rmc.setCourse(lastSOGCOG.getDouble("COG"));
            rmc.setSpeed(lastSOGCOG.getDouble("SOG"));
        }
        if (lastTime!=null) {
            Instant ts = lastTime.minusMillis(System.currentTimeMillis() - lastLocalTime);
            DateTimeFormatter fmDate = DateTimeFormatter.ofPattern("ddMMYY").withZone(ZoneId.of("UTC"));
            DateTimeFormatter fmTime = DateTimeFormatter.ofPattern("HHmmss").withZone(ZoneId.of("UTC"));
            rmc.setDate(new Date(fmDate.format(ts)));
            rmc.setTime(new Time(fmTime.format(ts)));
        }
        return rmc;
    }

    private Sentence handleSOGCOG(JSONObject fields) {
        lastSOGCOG = fields;
        return null;
    }

    private Sentence handleWaterTemp(JSONObject fields) {
        // {"timestamp":"2020-06-05-18:03:25.630","prio":5,"src":105,"dst":255,"pgn":130310,"description":"Environmental Parameters","fields":{"SID":0,"Water Temperature":20.80}}
        MTWSentence mtw = (MTWSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
        mtw.setTemperature(fields.getDouble("Water Temperature"));
        return mtw;
    }

    private Sentence handleHeading(JSONObject fields) {
        // {"timestamp":"2020-06-05-18:03:25.680","prio":2,"src":204,"dst":255,"pgn":127250,"description":"Vessel Heading","fields":{"Heading":279.5,"Reference":"Magnetic"}}
        double heading = fields.getDouble("Heading");
        String ref = fields.getString("Reference");
        switch (ref) {
            case "Magnetic":
                lastHeading = fields;
                HDMSentence hdm = (HDMSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                hdm.setHeading(heading);
                return hdm;
            case "True":
                HDTSentence hdt = (HDTSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
                hdt.setHeading(heading);
                return hdt;
            default:
                return null;
        }
    }

    private Sentence handleSpeed(JSONObject fields) {
        // {"timestamp":"2020-06-05-18:03:25.627","prio":2,"src":105,"dst":255,"pgn":128259,"description":"Speed","fields":{"SID":0,"Speed Water Referenced":0.00,"Speed Water Referenced Type":"Paddle wheel"}}
        VHWSentence vhw = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
        if (lastHeading!=null) {
            vhw.setMagneticHeading(lastHeading.getDouble("Heading"));
        }
        double speed = fields.getDouble("Speed Water Referenced");
        vhw.setSpeedKnots(speed);
        vhw.setSpeedKmh(speed * 1.852);
        return vhw;
    }

    private Sentence handleDepth(JSONObject fields) {
        double depth = fields.getDouble("Depth");
        double offset = fields.getDouble("Offset");
        DPTSentence dpt = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
        dpt.setOffset(offset);
        dpt.setDepth(depth);
        return dpt;
    }

    private Sentence handleWind(JSONObject jsonObject) {
        // mind that speed in m/s
        double windSpeed = jsonObject.getDouble("Wind Speed") * 1.94384;
        double windAngle = jsonObject.getDouble("Wind Angle");
        String ref = jsonObject.getString("Reference");
        MWVSentence mwv = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
        mwv.setAngle(windAngle);
        mwv.setSpeed(windSpeed);
        mwv.setSpeedUnit(Units.KNOT);
        mwv.setTrue(false);
        mwv.setStatus(DataStatus.ACTIVE);
        return mwv;
    }


}
