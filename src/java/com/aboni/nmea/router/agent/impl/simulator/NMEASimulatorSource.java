package com.aboni.nmea.router.agent.impl.simulator;

import com.aboni.geo.ApparentWind;
import com.aboni.misc.PolarTable;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.seatalk.Stalk84;
import com.aboni.utils.Constants;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class NMEASimulatorSource extends NMEAAgentImpl {

	private int headingAuto = Integer.MIN_VALUE;
	private double refHeading = Double.NaN;
	private PolarTable polars;
	
	private static NMEASimulatorSource simulator;

	@Nonnull
	private final NMEASimulatorSourceSettings data;
    
    private final TalkerId id;
    private final Random r = new Random();
	private Position pos = new Position(43.9599, 09.7745);
	private double distance = 0;
	private double trip = 0;
	private long lastTS = 0;

	public static NMEASimulatorSource getSimulator() {
		return simulator;
	}

	public static NMEASimulatorSource create(NMEACache cache, String name, QOS qos) {
		NMEASimulatorSource sim = new NMEASimulatorSource(cache, name, qos);
		if (simulator==null) simulator = sim;
		return sim;
	}

	private NMEASimulatorSource(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
		data = new NMEASimulatorSourceSettings("sim_" + name + ".properties");
        setSourceTarget(true, true);
        id = TalkerId.GP;
        polars = null;
	}
	
	private String lastPolarFile;


    @Override
    public String getType() {
    	return "Simulator";
    }

	private void loadPolars() {
		if (polars == null) {
			polars = new PolarTable();
		}
        try {
        	if (data.getPolars()!=null && !data.getPolars().equals(lastPolarFile)) {
				File f = new File(Constants.CONF_DIR, data.getPolars());
				try (FileReader reader = new FileReader(f)) {
					polars.load(reader);
				}
				lastPolarFile = data.getPolars();
        	}
		} catch (Exception e) {
			getLogger().error("Cannot load polars", e);
		}		
	}
	
	@Override
	protected boolean onActivate() {
		return true;
	}
	    
	@Override
	public String getDescription() {
		return "";
	}

    
    /*	
	------------------------------------------------------------------------------------------------------
	Measured Value | Transducer Type | Measured Data                   | Unit of measure | Transducer Name
	------------------------------------------------------------------------------------------------------
	barometric     | "P" pressure    | 0.8..1.1 or 800..1100           | "B" bar         | "Barometer"
	air temperature| "C" temperature |   2 decimals                    | "C" celsius     | "TempAir" or "ENV_OUTAIR_T"
	pitch          | "A" angle       |-180..0 nose down 0..180 nose up | "D" degrees     | "PTCH"
	rolling        | "A" angle       |-180..0 L         0..180 R       | "D" degrees     | "ROLL"
	water temp     | "C" temperature |   2 decimals                    | "C" celsius     | "ENV_WATER_T"
	-----------------------------------------------------------------------------------------------------
     */
   
	public double getHeading() {
		return refHeading;
	}

    public void setHeading(double heading) {
		this.refHeading = heading;
	}

    public double getwSpeed() {
		return data.getwSpeed();
	}

    public void setwSpeed(double wSpeed) {
		this.data.setwSpeed(wSpeed);
	}

    public double getwDirection() {
		return data.getwDirection();
	}

    public void setwDirection(double wDirection) {
		this.data.setwDirection(wDirection);
	}
	
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    @Override
    public void onTimer() {
    	doIt();
    }

	
	private void doIt() {
		if (isStarted()) {
			data.loadConf();
			loadPolars();
	
			Position posOut = new Position(pos.getLatitude(), pos.getLongitude());
			
			refHeading = data.getHeading();
			
			long newTS = System.currentTimeMillis();
			double ph15m = System.currentTimeMillis() / (1000d*60d*15d) * 2 * Math.PI; // 15 minutes phase
			double depth = round(data.getDepth() + Math.sin(ph15m) * data.getDepthRange(), 1);
			double hdg = Utils.normalizeDegrees0To360(refHeading + r.nextDouble() * 3.0);
			double absoluteWindSpeed = data.getwSpeed() + r.nextDouble() * 1.0;
			double absoluteWindDir = data.getwDirection() + r.nextDouble() * 2.0;
			double tWDirection = 	Utils.normalizeDegrees0To360(absoluteWindDir - hdg);
			double speed = getSpeed((float) absoluteWindSpeed, (int) tWDirection);

			distance += speed / 3600.0;
			trip += speed / 3600.0;
			
			ApparentWind aWind = new ApparentWind(speed, tWDirection, absoluteWindSpeed);
			double aWSpeed = 		aWind.getApparentWindSpeed();
			double aWDirection = 	Utils.normalizeDegrees0To360(aWind.getApparentWindDeg());
			
			double temp = round(data.getTemp() + (new Random().nextDouble()/10.0), 2);
			double press = round(data.getPress() + (new Random().nextDouble()/10.0), 1);
	        double roll = round(new Random().nextDouble()*5, 1);
	        double pitch = round((new Random().nextDouble()*5) + 0, 1);
	
	        if (lastTS!=0) {
	        	double dTime = (double)(newTS-lastTS) / 1000d / 60d / 60d;
	        	pos = Utils.calcNewLL(pos, hdg, speed * dTime);
	        	posOut = new Position(pos.getLatitude(), pos.getLongitude());
				addGpsNoise(posOut);
			}
			lastTS = newTS;

			sendVHW(hdg, speed);
			sendVLW();
			sendGLL(posOut);
			sendRMC(posOut, hdg, speed);
			sendDepth(depth);
			sendWind(absoluteWindSpeed, tWDirection, aWSpeed, aWDirection);
			sendHeading(hdg);
			sendVTG(hdg, speed);
			sendDeprecatedMeteo(hdg, absoluteWindSpeed, tWDirection, temp, press);
			sendGyro(hdg, roll, pitch);
			sendMeteo(temp, press);
			sendVoltage();
			sendAP();
			sendRSA();
		}		
	}

	private void addGpsNoise(Position posOut) {
		if (data.isGpsOut()) {
			int x = r.nextInt(25);
			if (x == 0) {
				if (r.nextBoolean())
					posOut.setLongitude(pos.getLongitude() + 1.0);
				else
					posOut.setLatitude(pos.getLatitude() + 1.0);
			}
		}
	}

	private double getSpeed(float absoluteWindSpeed, int tWDirection) {
		double speed;
		if (data.isUsePolars()) {
			speed = polars.getSpeed(tWDirection, absoluteWindSpeed) * data.getPolarCoeff();
		} else {
			speed = round(data.getSpeed() * (1.0 + r.nextDouble()/10.0), 1);
		}
		return speed;
	}

	private void sendAP() {
		if (data.isAutoPilot()) {
			sendAutopilotStatus();
		}
	}

	private void sendRSA() {
		if (data.isRsa()){
			RSASentence rsa = (RSASentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.RSA);
			rsa.setRudderAngle(Side.STARBOARD, data.getRudder());
			NMEASimulatorSource.this.notify(rsa);
		}
	}

	private void sendVoltage() {
		if (data.isXdrDiag()) {
			XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
			xdr.addMeasurement(new Measurement("V", 13.56, "V", "V0"));
			xdr.addMeasurement(new Measurement("V", 13.12, "V", "V1"));
			NMEASimulatorSource.this.notify(xdr);
		}
	}

	private void sendMeteo(double temp, double press) {
		if (data.isXdrMeteo()) {
            XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());

            if (data.isXdrMeteoAtm()) xdr.addMeasurement(new Measurement("P", press / 1000, "B", "Barometer"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp"));
            if (data.isXdrMeteoTmp()) xdr.addMeasurement(new Measurement("C", temp + 1, "C", "CabinTemp"));
            if (data.isXdrMeteoHum()) xdr.addMeasurement(new Measurement("C", data.getHum(), "H", "Humidity"));
            NMEASimulatorSource.this.notify(xdr);
        }
	}

	private void sendGyro(double hdg, double roll, double pitch) {
		if (data.isXdrGYR()) {
			XDRSentence xdr = (XDRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
			xdr.addMeasurement(new Measurement("A", hdg, "D", "HEAD"));
			xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
			xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
			NMEASimulatorSource.this.notify(xdr);
		}
	}

	private void sendDeprecatedMeteo(double hdg, double absoluteWindSpeed, double tWDirection, double temp, double press) {
		if (data.isMtw()) {
            MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
            t.setTemperature(temp - 5);
            NMEASimulatorSource.this.notify(t);
        }

		if (data.isMta()) {
			MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
			mta.setTemperature(temp);
			NMEASimulatorSource.this.notify(mta);
		}

		if (data.isMbb()) {
            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
            mmb.setBars(press / 1000.0);
            NMEASimulatorSource.this.notify(mmb);
        }

		if (data.isMhu()) {
			MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
			mhu.setRelativeHumidity(data.getHum());
			NMEASimulatorSource.this.notify(mhu);
		}

		if (data.isMda()) {
            MDASentence mda = (MDASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MDA");
            mda.setRelativeHumidity(data.getHum());
            mda.setAirTemperature(temp);
            mda.setPrimaryBarometricPressure(press * 750.06375541921);
            mda.setPrimaryBarometricPressureUnit('I');
            mda.setSecondaryBarometricPressure(press / 1000.0);
            mda.setSecondaryBarometricPressureUnit('B');
            mda.setWaterTemperature(temp - 5);
            mda.setMagneticWindDirection(tWDirection + hdg);
            mda.setTrueWindDirection(tWDirection + hdg);
            mda.setWindSpeedKnots(absoluteWindSpeed);
            NMEASimulatorSource.this.notify(mda);
        }
	}

	private void sendVTG(double hdg, double speed) {
		if (data.isVtg()) {
			VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VTG);
			vtg.setMagneticCourse(hdg);
			vtg.setTrueCourse(hdg);
			vtg.setMode(FaaMode.AUTOMATIC);
			vtg.setSpeedKnots(speed);
			vtg.setSpeedKmh(speed * 1.852);
			NMEASimulatorSource.this.notify(vtg);
		}
	}

	private void sendHeading(double hdg) {
		if (data.isHdm()) {
			HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
			hdm.setHeading(hdg);
			NMEASimulatorSource.this.notify(hdm);
		}

		if (data.isHdt()) {
			HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
			hdt.setHeading(hdg);
			NMEASimulatorSource.this.notify(hdt);
		}

		if (data.isHdg()) {
			HDGSentence hdgS = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
			hdgS.setHeading(hdg);
			NMEASimulatorSource.this.notify(hdgS);
		}
	}

	private void sendSplitApparentWind(double aWSpeed, double aWDirection) {
		MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
		v.setAngle(aWDirection);
		v.setTrue(false);
		v.setSpeedUnit(Units.KNOT);
		v.setStatus(DataStatus.ACTIVE);
		NMEASimulatorSource.this.notify(v);

		v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
		v.setTrue(false);
		v.setSpeedUnit(Units.KMH);
		v.setSpeed(aWSpeed * 1.852);
		v.setStatus(DataStatus.ACTIVE);
		NMEASimulatorSource.this.notify(v);
	}

	private void sendWind(double absoluteWindSpeed, double tWDirection, double aWSpeed, double aWDirection) {
		if (data.isMwvA()) {
			if (data.isSplitWind()) {
				sendSplitApparentWind(aWSpeed, aWDirection);
			} else {
				sendApparentWind(aWSpeed, aWDirection, false);
			}
		}

		if (data.isMwvT()) {
			sendApparentWind(absoluteWindSpeed, tWDirection, true);
		}

		if (data.isVwr()) {
			VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWR");
			vwr.setAngle(aWDirection>180?360-aWDirection:aWDirection);
			vwr.setSpeed(aWSpeed);
			vwr.setSide(Side.PORT);
			vwr.setStatus(DataStatus.ACTIVE);
			NMEASimulatorSource.this.notify(vwr);
		}

		if (data.isVwr()) {
			VWTSentence vwt = (VWTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWT");
			vwt.setWindAngle(tWDirection > 180 ? 360 - tWDirection : tWDirection);
			vwt.setSpeedKnots(absoluteWindSpeed);
			vwt.setDirectionLeftRight(tWDirection > 180 ? Direction.LEFT : Direction.RIGHT);
			NMEASimulatorSource.this.notify(vwt);
		}
	}

	private void sendApparentWind(double aWSpeed, double aWDirection, boolean b) {
		MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
		if (data.isWindSpeedInMS()) {
			v.setSpeedUnit(Units.METER);
			v.setSpeed(aWSpeed / 1.947);
		} else {
			v.setSpeedUnit(Units.KNOT);
			v.setSpeed(aWSpeed);
		}
		v.setAngle(aWDirection);
		v.setTrue(b);
		v.setStatus(DataStatus.ACTIVE);
		NMEASimulatorSource.this.notify(v);
	}

	private void setWindValues(double aWSpeed, MWVSentence v) {
		if (data.isWindSpeedInMS()) {
			v.setSpeedUnit(Units.METER);
			v.setSpeed(aWSpeed / 1.947);
		} else {
			v.setSpeedUnit(Units.KNOT);
			v.setSpeed(aWSpeed);
		}
	}

	private void sendDepth(double depth) {
		if (data.isDpt()) {
			DPTSentence d = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
			d.setDepth(depth);
			d.setOffset(data.getDepthOffset());
			NMEASimulatorSource.this.notify(d);
		}

		if (data.isDbt()) {
			DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DBT);
			d.setDepth(depth);
			NMEASimulatorSource.this.notify(d);
		}
	}

	private void sendRMC(Position posOut, double hdg, double speed) {
		if (data.isRmc()) {
			RMCSentence rmc = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
			rmc.setCourse(hdg);

			rmc.setStatus(DataStatus.ACTIVE);

			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Date ddd = new Date();
			ddd.setDay(c.get(Calendar.DAY_OF_MONTH));
			ddd.setMonth(c.get(Calendar.MONTH)+1);
			ddd.setYear(c.get(Calendar.YEAR));
			rmc.setDate(ddd);
			Time ttt = new Time();
			ttt.setHour(c.get(Calendar.HOUR_OF_DAY));
			ttt.setMinutes(c.get(Calendar.MINUTE));
			ttt.setSeconds(c.get(Calendar.SECOND));
			rmc.setTime(ttt);

			rmc.setVariation(0.0);
			rmc.setMode(FaaMode.AUTOMATIC);
			rmc.setDirectionOfVariation(CompassPoint.WEST);
			rmc.setSpeed(speed);
			rmc.setPosition(posOut);
			NMEASimulatorSource.this.notify(rmc);
		}
	}

	private void sendGLL(Position posOut) {
		if (data.isGll()) {
			GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
			s1.setPosition(posOut);
			s1.setStatus(DataStatus.ACTIVE);
			s1.setTime(new Time());
			NMEASimulatorSource.this.notify(s1);
		}
	}

	private void sendVLW() {
		if (data.isVlw()) {
			VLWSentence s = (VLWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VLW);
			s.setTotal(distance);
			s.setTotalUnits('N');
			s.setTrip(trip);
			s.setTripUnits('N');
			NMEASimulatorSource.this.notify(s);
		}
	}

	private void sendVHW(double hdg, double speed) {
		if (data.isVhw()) {
			VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
			s.setHeading(hdg);
			s.setMagneticHeading(hdg);
			s.setSpeedKnots(speed);
			s.setSpeedKmh(speed * 1.852);
			NMEASimulatorSource.this.notify(s);
		}
	}

	private void sendAutopilotStatus() {
		Stalk84 s84 = new Stalk84(
				(int)refHeading, (headingAuto==Integer.MIN_VALUE)?0:headingAuto, 0,
				(headingAuto==Integer.MIN_VALUE)?Stalk84.STATUS.STATUS_STANDBY:Stalk84.STATUS.STATUS_AUTO,
				Stalk84.ERROR.ERROR_NONE, Stalk84.TURN.STARBOARD);				
		STALKSentence stalk = (STALKSentence)SentenceFactory.getInstance().createParser(s84.getSTALKSentence());
		NMEASimulatorSource.this.notify(stalk);
	}
	
    @Override
    protected void doWithSentence(Sentence s, String source) {
    	if (!source.equals(getName()) && s instanceof STALKSentence) {
			STALKSentence t = (STALKSentence) s;
			if ("86".equals(t.getCommand())) {
				String[] p = t.getParameters();
				if ("21".equals(p[0])) {
					handleAPStatusCommands(p);
				} else if ("11".equals(p[0])) {
					handleAPDirectionCommands(p);
				}
			}

		}
    		
    }

	private void handleAPStatusCommands(String[] p) {
		String disc = p[1] + p[2];
		switch (disc) {
			case "01FE":
			if (headingAuto==Integer.MIN_VALUE) {
				headingAuto = (int)refHeading;
				sendAutopilotStatus();
			}
			break;
			case "02FD":
			if (headingAuto!=Integer.MIN_VALUE) {
				headingAuto = Integer.MIN_VALUE;
				sendAutopilotStatus();
			}
			break;
			default: break;
		}
	}

	private void  handleAPDirectionCommands(String[] p) {
		String disc = p[1] + p[2];
		int delta;
		switch (disc) {
			case "05FA": delta = -1; break;
			case "06F9": delta = -10; break;
			case "07F8": delta = 1; break;
			case "08F7": delta = 10; break;
			default: delta = 0; break;
		}
		if (delta!=0) {
			if (headingAuto != Integer.MIN_VALUE) {
				headingAuto += delta;
				sendAutopilotStatus();
			}
			refHeading += delta;
		}
	}

	public double getSpeed() {
		return data.getSpeed();
	}

	public void setSpeed(double speed) {
		this.data.setSpeed(speed);
	}
}
