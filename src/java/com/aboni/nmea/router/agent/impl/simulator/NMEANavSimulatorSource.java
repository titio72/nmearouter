package com.aboni.nmea.router.agent.impl.simulator;

import com.aboni.geo.NavSimulator;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

@SuppressWarnings("ALL")
public class NMEANavSimulatorSource extends NMEAAgentImpl {

	public static NMEANavSimulatorSource SIMULATOR;
	
	private final NavSimulator sim = new NavSimulator();

	public NMEANavSimulatorSource(NMEACache cache, String name) {
		this(cache, name, null);
	}
	
	public NMEANavSimulatorSource(NMEACache cache, String name, QOS qos) {
		super(cache, name, qos);
        setSourceTarget(true, false);
        
		Position marina = new Position(43.679416, 10.267679);
		Position capraia = new Position(43.051326, 9.839279);
		NavSimulator sim = new NavSimulator();
		try { sim.loadPolars("web/dufour35c.csv"); } catch (Exception e) { e.printStackTrace(); }
		sim.setFrom(marina);
		sim.setTo(capraia);
		sim.setWind(9.0,  205.0);
        
        
        
        if (SIMULATOR!=null) throw new RuntimeException();
        else SIMULATOR = this;
	}
	
	@Override
	protected boolean onActivate() {
		doSimulate();
		return true;
	}
	    
	@Override
	protected void onDeactivate() {
	}
	
	@Override
	public String getDescription() {
		return "";
	}
   
	public double getHeading() {
		return sim.getHeading();
	}

    public void setHeading(double _heading) {
		sim.setHeading(_heading);
	}

    public double getwSpeed() {
		return sim.getWindSpeed();
	}

    public void setWind(double wSpeed, double wDir) {
		sim.setWind(wSpeed, wDir);
	}

    public double getwDirection() {
		return sim.getWindDir();
	}
	
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
	private void doSimulate() {
		new Thread(() -> {
			TalkerId id = TalkerId.GP;
			Random r = new Random();

			Position pos = new Position(43.9599, 09.7745);

			while (isStarted()) {
				try {
					Thread.sleep(1000);

					sim.doCalc(System.currentTimeMillis());

					double heading = sim.getHeading();
					double speed = sim.getSpeed();

					//double absoluteWindSpeed = sim.getWindSpeed();
					//double absoluteWindDir = sim.getWindDir();

					double tWSpeed = 		sim.getWindTrueSpeed();
					double tWDirection = 	sim.getWindTrue();

					double aWSpeed = 		sim.getWindAppSpeed();
					double aWDirection = 	sim.getWindApp();

double roll = round(new Random().nextDouble()*5, 1);
double pitch = round((new Random().nextDouble()*5) + 0, 1);

					VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VHW);
s.setHeading(heading);
s.setMagneticHeading(Utils.normalizeDegrees0_360(heading + r.nextDouble() * 1.5));
					s.setSpeedKnots(speed);
					s.setSpeedKmh(speed * 1.852);
					NMEANavSimulatorSource.this.notify(s);

					GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(id, SentenceId.GLL);
					s1.setPosition(pos);
					s1.setStatus(DataStatus.ACTIVE);
					s1.setTime(new Time());
					NMEANavSimulatorSource.this.notify(s1);

					RMCSentence rmc = (RMCSentence)SentenceFactory.getInstance().createParser(id, SentenceId.RMC);
					rmc.setCourse(heading);

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
					rmc.setMode(FaaMode.SIMULATED);
					rmc.setDirectionOfVariation(CompassPoint.WEST);
					rmc.setSpeed(speed);
					rmc.setPosition(pos);
					NMEANavSimulatorSource.this.notify(rmc);

MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
v.setSpeedUnit(Units.KNOT);
v.setAngle(aWDirection);
v.setSpeed(aWSpeed);
v.setTrue(false);
NMEANavSimulatorSource.this.notify(v);

MWVSentence vt = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
vt.setSpeedUnit(Units.KNOT);
vt.setAngle(tWDirection);
vt.setSpeed(tWSpeed);
vt.setTrue(true);
NMEANavSimulatorSource.this.notify(vt);

VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(id, "VWR");
vwr.setAngle(aWDirection>180?360-aWDirection:aWDirection);
vwr.setSpeed(aWSpeed);
vwr.setSide(Side.PORT);
vwr.setStatus(DataStatus.ACTIVE);
NMEANavSimulatorSource.this.notify(vwr);

HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDM);
hdm.setHeading(heading);
NMEANavSimulatorSource.this.notify(hdm);

HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDT);
hdt.setHeading(heading);
NMEANavSimulatorSource.this.notify(hdt);

HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDG);
hdg.setHeading(heading);
//hdg.setDeviation(0.0);
NMEANavSimulatorSource.this.notify(hdg);

VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VTG);
vtg.setMagneticCourse(heading);
vtg.setTrueCourse(heading);
vtg.setMode(FaaMode.AUTOMATIC);
vtg.setSpeedKnots(speed);
vtg.setSpeedKmh(speed * 1.852);
NMEANavSimulatorSource.this.notify(vtg);

XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());                        xdr.addMeasurement(new Measurement("A", heading, "D", "HEAD"));
xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
NMEANavSimulatorSource.this.notify(xdr);
				} catch (InterruptedException e) {
					ServerLog.getLogger().Error("Error simulating", e);
					e.printStackTrace();
				}
			}

		}).start();

			
	}

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    }

	public double getSpeed() {
		return sim.getSpeed();
	}
}
