package com.aboni.nmea.router.agent.impl.simulator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import com.aboni.geo.ApparentWind;
import com.aboni.geo.NavSimulator;
import com.aboni.misc.Utils;
import com.aboni.utils.ServerLog;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.FaaMode;
import net.sf.marineapi.nmea.util.Measurement;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Side;
import net.sf.marineapi.nmea.util.Time;
import net.sf.marineapi.nmea.util.Units;

public class NMEASimulatorSource extends NMEAAgentImpl {

	public static NMEASimulatorSource SIMULATOR;
    private NMEASimulatorSourceSettings data = new NMEASimulatorSourceSettings();

	public NMEASimulatorSource(NMEACache cache, NMEAStream stream, String name) {
		this(cache, stream, name, null);
	}
	
	public NMEASimulatorSource(NMEACache cache, NMEAStream stream, String name, QOS qos) {
		super(cache, stream, name, qos);
        setSourceTarget(true, false);
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
		return data._heading;
	}

    public void setHeading(double _heading) {
		this.data._heading = _heading;
	}

    public double getwSpeed() {
		return data._wSpeed;
	}

    public void setwSpeed(double wSpeed) {
		this.data._wSpeed = wSpeed;
	}

    public double getwDirection() {
		return data._wDirection;
	}

    public void setwDirection(double wDirection) {
		this.data._wDirection = wDirection;
	}
	
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
	private void doSimulate() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				TalkerId id = TalkerId.GP;
				Random r = new Random();
				
				Position pos = new Position(43.9599, 09.7745);
				long lastTS = 0;
				
				while (isStarted()) {
					try {
						Thread.sleep(1000);
						data.loadConf();
					
						long newTS = System.currentTimeMillis();
						double ph15m = System.currentTimeMillis() / (1000d*60d*15d) * 2 * Math.PI; // 15 minutes phase
						double ph1h = System.currentTimeMillis() / (1000d*60d*60d*1d) * 2 * Math.PI; // 1h phase
						double depth = round(10.0 + Math.sin(ph15m)*5.0, 1);
						double heading = Utils.normalizeDegrees0_360(data._heading * Math.cos(ph1h) + r.nextDouble() * 3.0);
						double speed = round(data._speed * (1.0 + r.nextDouble()/10.0), 1);
						
						double absoluteWindSpeed = data._wSpeed + r.nextDouble() * 1.0; 
						double absoluteWindDir = data._wDirection + r.nextDouble() * 2.0 + Math.cos(ph1h) * 15.0; 
						
						double tWSpeed = 		absoluteWindSpeed;
						double tWDirection = 	Utils.normalizeDegrees0_360(absoluteWindDir - heading);

						ApparentWind aWind = new ApparentWind(speed, tWDirection, tWSpeed);
						double aWSpeed = 		aWind.getApparentWindSpeed();
						double aWDirection = 	Utils.normalizeDegrees0_360(aWind.getApparentWindDeg());
						
						double temp = round(data._temp + (new Random().nextDouble()/10.0), 2);
						double press = round(data._press + (new Random().nextDouble()/10.0), 1);
                        double roll = round(new Random().nextDouble()*5, 1);
                        double pitch = round((new Random().nextDouble()*5) + 0, 1);

                        if (lastTS!=0) pos = NavSimulator.calcNewLL(pos, heading, speed * (double)(newTS-lastTS) / 1000d / 60d / 60d);
						lastTS = newTS;
						
						if (data._vhw) {
    						VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VHW);
                            s.setHeading(heading);
                            s.setMagneticHeading(Utils.normalizeDegrees0_360(heading + r.nextDouble() * 1.5));
    						s.setSpeedKnots(speed);
    						s.setSpeedKmh(speed * 1.852);
    						NMEASimulatorSource.this.notify(s);
						}
						
						if (data._gll) {
    						GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(id, SentenceId.GLL);
    						s1.setPosition(pos);
    						s1.setStatus(DataStatus.ACTIVE);
    						s1.setTime(new Time());
    						NMEASimulatorSource.this.notify(s1);
						}
						
						if (data._rmc) {
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
    						NMEASimulatorSource.this.notify(rmc);
						}

						if (data._dpt) {
    						DPTSentence d = (DPTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.DPT);
    						d.setDepth(depth); 
    						NMEASimulatorSource.this.notify(d);
						}
						
						if (data._dbt) {
    						DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.DBT);
    						d.setDepth(depth - 0.2);
    						NMEASimulatorSource.this.notify(d);
						}
						
						if (data._mtw) {
    						MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MTW);
    						t.setTemperature(28.5);
    						NMEASimulatorSource.this.notify(t);
						}
						
						if (data._mwv_a) {
                            MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
                            v.setSpeedUnit(Units.KNOT);
                            v.setAngle(aWDirection);
                            v.setSpeed(aWSpeed);
                            v.setTrue(false);
                            NMEASimulatorSource.this.notify(v);
						}
                        
						if (data._mwv_t) {
                            MWVSentence vt = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
                            vt.setSpeedUnit(Units.KNOT);
                            vt.setAngle(tWDirection);
                            vt.setSpeed(tWSpeed);
                            vt.setTrue(true);
                            NMEASimulatorSource.this.notify(vt);
						}
                        
						if (data._vwr) {
                            VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(id, "VWR");
                            vwr.setAngle(aWDirection>180?360-aWDirection:aWDirection);
                            vwr.setSpeed(aWSpeed);
                            vwr.setSide(Side.PORT);
                            vwr.setStatus(DataStatus.ACTIVE);
                            NMEASimulatorSource.this.notify(vwr);
						}
                        
                        if (data._hdm) {
                            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDM);
                            hdm.setHeading(heading);
                            NMEASimulatorSource.this.notify(hdm);
                        }
                        
                        if (data._hdt) {
                            HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDT);
                            hdt.setHeading(heading);
                            NMEASimulatorSource.this.notify(hdt);
                        }
                        
                        if (data._hdg) {
                            HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDG);
                            hdg.setHeading(heading);
                            //hdg.setDeviation(0.0);
                            NMEASimulatorSource.this.notify(hdg);
                        }
                        
                        if (data._vtg) {
                            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VTG);
                            vtg.setMagneticCourse(heading);
                            vtg.setTrueCourse(heading);
                            vtg.setMode(FaaMode.AUTOMATIC);
                            vtg.setSpeedKnots(speed);
                            vtg.setSpeedKmh(speed * 1.852);
                            NMEASimulatorSource.this.notify(vtg);
                        }
                        
                        if (data._mta) {
                            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(id, "MTA");
                            mta.setTemperature(temp);;
                            NMEASimulatorSource.this.notify(mta);
                        }
                        
                        if (data._mbb) {
                            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(id, "MMB");
                            mmb.setBars(press/1000.0);
                            NMEASimulatorSource.this.notify(mmb);
                        }
                        
                        if (data._mhu) {
                            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(id, "MHU");
                            mhu.setRelativeHumidity(data._hum);
                            NMEASimulatorSource.this.notify(mhu);
                        }
                        
                        if (data._xdrGYR) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                        xdr.addMeasurement(new Measurement("A", heading, "D", "HEAD"));
	                        xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
	                        xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
                        if (data._xdrMeteo) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());

	                        if (data._xdrMeteoAtm) xdr.addMeasurement(new Measurement("P", press, "B", "Barometer_0"));
	                        if (data._xdrMeteoTmp) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp_0"));
	                        if (data._xdrMeteoHum) xdr.addMeasurement(new Measurement("C", data._hum, "H", "Humidity_0"));
	                        if (data._xdrMeteoAtm) xdr.addMeasurement(new Measurement("P", press, "B", "Barometer_1"));
	                        if (data._xdrMeteoTmp) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp_1"));
	                        if (data._xdrMeteoHum) xdr.addMeasurement(new Measurement("C", data._hum, "H", "Humidity_1"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
                        
                        if (data._xdrDiag) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                        xdr.addMeasurement(new Measurement("V", 13.56, "V", "V0"));
	                        xdr.addMeasurement(new Measurement("V", 13.12, "V", "V1"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
					} catch (InterruptedException e) {
						ServerLog.getLogger().Error("Error simulating", e);
						e.printStackTrace();
					}
				}
				
			}
		}).start();

			
	}

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    }

	public double getSpeed() {
		return data._speed;
	}

	public void setSpeed(double speed) {
		this.data._speed = speed;
	}
}
