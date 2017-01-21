package com.aboni.nmea.router.agent;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import com.aboni.geo.TrueWind;
import com.aboni.geo.Utils;
import com.aboni.sensors.MagnetometerToCompass;
import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.MMBSentence;
import com.aboni.nmea.sentences.MTASentence;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.nmea.sentences.XXXPSentence;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
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
	
	public NMEASimulatorSource(String name) {
		this(name, null);
	}
	
	public NMEASimulatorSource(String name, QOS qos) {
		super(name, qos);
        setSourceTarget(true, true);
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

    private double rec_heading = Double.NaN;
	
    private double getRec_heading() {
        synchronized (this) {
            return rec_heading;
        }
    }

    private void setRec_heading(double rec_heading) {
        synchronized (this) {
            this.rec_heading = rec_heading;
        }
    }

    boolean _vhw   = true;  // water spead and heading
    boolean _gll   = true;  // gps
    boolean _rmc   = true;  // gps
    boolean _dpt   = true;  // depth
    boolean _dbt   = true;  // depth
    boolean _mtw   = true;  // water temp
    boolean _mta   = false;  // air temp
    boolean _mbb   = false;  // atm pressure
    boolean _mwv_a = true;  // wind apparent
    boolean _mwv_t = true;  // wind true
	boolean _vwr   = true;  // relative wind speed and angle (apparent)
    boolean _hdm   = true; // magn heading
    boolean _hdg   = true;  // magn heading + variation/deviation
    boolean _hdt   = true; // true heading
    boolean _xxx   = false;  // sensors
    boolean _vtg   = true;  // cog-sog
    boolean _xdrDiag = true;
    boolean _xdrMeteo = true;
    boolean _xdrMeteoAtm = true;
    boolean _xdrMeteoHum = true;
    boolean _xdrMeteoTmp = true;
    boolean _xdrGYR = true;
    
    private double _speed = 5.9;
    private double _wSpeed = 11;
    private double _wDirection = 34;
	private double _heading = 354;
	
    public double getHeading() {
		return _heading;
	}

    public void setHeading(double _heading) {
		this._heading = _heading;
	}

    public double getwSpeed() {
		return _wSpeed;
	}

    public void setwSpeed(double wSpeed) {
		this._wSpeed = wSpeed;
	}

    public double getwDirection() {
		return _wDirection;
	}

    public void setwDirection(double wDirection) {
		this._wDirection = wDirection;
	}

	
	private void doSimulate() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				TalkerId id = TalkerId.GP;
				MagnetometerToCompass mag = new MagnetometerToCompass();
				Random r = new Random();
				int i = 1;
				while (isStarted()) {
					try {
						i++;
						Thread.sleep(1000);
                        double temp = 29.5;
                        double press = 1030;
                        double hum = 48.1;
                        
						double rh = getRec_heading();
						double heading = Double.isNaN(rh)?(345 + r.nextDouble() * 3.0):rh;

						
						double speed = _speed + r.nextDouble() * 0.5;
						double wSpeed = _wSpeed + Math.sin(i/10.0 * Math.PI);
						double wDirection = _wDirection + r.nextDouble() * 3.0;
						TrueWind trueWind = new TrueWind(speed, wDirection, wSpeed);
						
						if (_vhw) {
    						VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VHW);
                            s.setHeading(heading);
                            s.setMagneticHeading(Utils.normalizeDegrees0_360(heading + r.nextDouble() * 1.5));
    						s.setSpeedKnots(speed);
    						s.setSpeedKmh(speed * 1.852);
    						NMEASimulatorSource.this.notify(s);
						}
						
						if (_gll) {
    						GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(id, SentenceId.GLL);
    						s1.setPosition(new Position(43.63061, 10.29333));
    						s1.setStatus(DataStatus.ACTIVE);
    						s1.setTime(new Time());
    						NMEASimulatorSource.this.notify(s1);
						}
						
						if (_rmc) {
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
    						rmc.setPosition(new Position(43.63061, 10.29333));
    						NMEASimulatorSource.this.notify(rmc);
						}

						double ph = System.currentTimeMillis() / (1000d*60d*15d) * 2 * Math.PI; // 15 minutes phase
						double depth = 10.0 + Math.sin(ph)*5.0;

						if (_dpt) {
    						DPTSentence d = (DPTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.DPT);
    						d.setDepth(depth); 
    						NMEASimulatorSource.this.notify(d);
						}
						

						if (_dbt) {
    						DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.DBT);
    						d.setDepth(depth - 0.2);
    						NMEASimulatorSource.this.notify(d);
						}
						
						if (_mtw) {
    						MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MTW);
    						t.setTemperature(28.5);
    						NMEASimulatorSource.this.notify(t);
						}
						
						if (_mwv_a) {
                            MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
                            v.setSpeedUnit(Units.KNOT);
                            v.setAngle(wDirection);
                            v.setSpeed(wSpeed);
                            v.setTrue(false);
                            NMEASimulatorSource.this.notify(v);
						}
                        
						if (_mwv_t) {
                            MWVSentence vt = (MWVSentence) SentenceFactory.getInstance().createParser(id, SentenceId.MWV);
                            vt.setSpeedUnit(Units.KNOT);
                            vt.setAngle(Utils.normalizeDegrees0_360(trueWind.getTrueWindDeg()));
                            vt.setSpeed(trueWind.getTrueWindSpeed());
                            vt.setTrue(true);
                            NMEASimulatorSource.this.notify(vt);
						}
                        
						if (_vwr) {
                            VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(id, "VWR");
                            vwr.setAngle(wDirection);
                            vwr.setSpeed(wSpeed);
                            vwr.setSide(Side.PORT);
                            vwr.setStatus(DataStatus.ACTIVE);
                            NMEASimulatorSource.this.notify(vwr);
						}
                        
                        if (_hdm) {
                            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDM);
                            hdm.setHeading(heading);
                            NMEASimulatorSource.this.notify(hdm);
                        }
                        
                        if (_hdt) {
                            HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDT);
                            hdt.setHeading(heading);
                            NMEASimulatorSource.this.notify(hdt);
                        }
                        
                        if (_hdg) {
                            HDGSentence hdg = (HDGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.HDG);
                            hdg.setHeading(heading);
                            hdg.setDeviation(0.0);
                            NMEASimulatorSource.this.notify(hdg);
                        }
                        
                        if (_vtg) {
                            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(id, SentenceId.VTG);
                            vtg.setMagneticCourse(heading);
                            vtg.setTrueCourse(heading);
                            vtg.setMode(FaaMode.AUTOMATIC);
                            vtg.setSpeedKnots(speed);
                            vtg.setSpeedKmh(speed * 1.852);
                            NMEASimulatorSource.this.notify(vtg);
                        }
                        
                        if (_mta) {
                            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(id, "MTA");
                            mta.setTemperature(24.2 + (new Random().nextDouble()/10.0));;
                            NMEASimulatorSource.this.notify(mta);
                        }
                        
                        if (_mbb) {
                            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(id, "MMB");
                            mmb.setPresBar(1.023);
                            NMEASimulatorSource.this.notify(mmb);
                        }
                        
                        double a = i;
                        double xm = Math.cos(a) * 150 + r.nextDouble() * 5 + 75; 
                        double ym = Math.sin(a) * 150 + r.nextDouble() * 5 + 45;
                        double zm = 0;
                        double roll = (new Random().nextDouble()*5) + 28;
                        double pitch = (new Random().nextDouble()*5) + 0;
                        mag.getHeading(xm,  ym,  zm);
                        if (_xxx) {
	                        XXXPSentence xs = (XXXPSentence) SentenceFactory.getInstance().createParser(id, "XXP");
	                        mag.getHeading(xm,  ym,  zm);
	                        xs.setHeading(heading);
	                        xs.setMagX((int)xm);
	                        xs.setMagY((int)ym);
	                        xs.setMagZ((int)zm);
	                        xs.setPressure(1030);
	                        xs.setRotationX(roll);
	                        xs.setRotationY(pitch);
	                        xs.setRotationZ(0);
	                        xs.setTemperature(29.5);
	                        xs.setVoltage(13.56);
	                        xs.setVoltage1(5.12);
	                        xs.setRPM(2110);
                        	NMEASimulatorSource.this.notify(xs);
                        }
                        
                        if (_xdrGYR) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                        xdr.addMeasurement(new Measurement("A", heading, "D", "HEAD"));
	                        xdr.addMeasurement(new Measurement("A", roll, "D", "ROLL"));
	                        xdr.addMeasurement(new Measurement("A", pitch, "D", "PITCH"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
                        if (_xdrMeteo) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());

	                        if (_xdrMeteoAtm) xdr.addMeasurement(new Measurement("P", press, "B", "Barometer"));
	                        if (_xdrMeteoTmp) xdr.addMeasurement(new Measurement("C", temp, "C", "TempAir"));
	                        if (_xdrMeteoHum) xdr.addMeasurement(new Measurement("C", hum, "H", "Humidity"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
                        
                        if (_xdrDiag) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                        xdr.addMeasurement(new Measurement("V", 13.56, "V", "Voltage1"));
	                        xdr.addMeasurement(new Measurement("V", 13.12, "V", "Voltage2"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}).start();

			
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
	

    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
        if (s instanceof HDMSentence && source!=this) {
            setRec_heading(((HDMSentence)s).getHeading());
        }
    }

	public double getSpeed() {
		return _speed;
	}

	public void setSpeed(double speed) {
		this._speed = speed;
	}

	
}
