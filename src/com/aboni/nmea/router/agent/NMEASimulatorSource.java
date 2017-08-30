package com.aboni.nmea.router.agent;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;
import java.util.TimeZone;

import com.aboni.geo.TrueWind;
import com.aboni.geo.Utils;
import com.aboni.sensors.MagnetometerToCompass;
import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.nmea.sentences.XXXPSentence;

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

	private long lastConfModified = 0;
	
	public static NMEASimulatorSource SIMULATOR;
	
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
	
    private void loadConf() {
    	try {
    		File f = new File(Constants.SIM);
    		if (f.exists()) {
    			if (f.lastModified() > lastConfModified) {
    				lastConfModified = f.lastModified();
    				Properties p = new Properties();
    				FileInputStream fi = new FileInputStream(f);
    				p.load(fi);
    				fi.close();
    				readConf(p);
    			}
    		}
    	} catch (Exception e) {
			ServerLog.getLogger().Error("Error reading smulator conf", e);
		}
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
    private void readConf(Properties p) {
        _vhw   = p.getProperty("simulate.vhw", "0").equals("1");  // water spead and heading
        _gll   = p.getProperty("simulate.gll", "0").equals("1");  // gps
        _rmc   = p.getProperty("simulate.rmc", "0").equals("1");  // gps
        _dpt   = p.getProperty("simulate.dpt", "0").equals("1");  // depth
        _dbt   = p.getProperty("simulate.dbt", "0").equals("1");  // depth
        _mtw   = p.getProperty("simulate.mtw", "0").equals("1");  // water temp
        _mta   = p.getProperty("simulate.mta", "0").equals("1");  // air temp
        _mbb   = p.getProperty("simulate.mbb", "0").equals("1");  // atm pressure
        _mhu   = p.getProperty("simulate.mhu", "0").equals("1");  // humidity
        _mwv_a = p.getProperty("simulate.mwv.apparent", "0").equals("1");  // wind apparent
        _mwv_t = p.getProperty("simulate.mwv.true", "0").equals("1");  // wind true
    	_vwr   = p.getProperty("simulate.vwr", "0").equals("1");  // relative wind speed and angle (apparent)
        _hdm   = p.getProperty("simulate.hdm", "0").equals("1"); // magn heading
        _hdg   = p.getProperty("simulate.hdg", "0").equals("1");  // magn heading + variation/deviation
        _hdt   = p.getProperty("simulate.hdt", "0").equals("1"); // true heading
        _xxx   = p.getProperty("simulate.xxx", "0").equals("1");  // sensors
        _vtg   = p.getProperty("simulate.vtg", "0").equals("1");  // cog-sog
        _xdrDiag 		= p.getProperty("simulate.xdr.diag", "0").equals("1");
        _xdrMeteo 		= p.getProperty("simulate.xdr.meteo", "0").equals("1");
        _xdrMeteoAtm 	= p.getProperty("simulate.xdr.meteo.atm", "0").equals("1");
        _xdrMeteoHum 	= p.getProperty("simulate.xdr.meteo.hum", "0").equals("1");
        _xdrMeteoTmp 	= p.getProperty("simulate.xdr.meteo.tmp", "0").equals("1");
        _xdrGYR 		= p.getProperty("simulate.xdr.gyro", "0").equals("1");
        
        try { _speed = Double.parseDouble(p.getProperty("simulate.speed", "5.9")); } catch (Exception e) {}
        try { _wSpeed = Double.parseDouble(p.getProperty("simulate.wSpeed", "11.1")); } catch (Exception e) {}
        try { _wDirection = Double.parseDouble(p.getProperty("simulate.wDirection", "34")); } catch (Exception e) {}
        try { _heading = Double.parseDouble(p.getProperty("simulate.heading", "354")); } catch (Exception e) {}
        try { _press = Double.parseDouble(p.getProperty("simulate.pressure", "1013")); } catch (Exception e) {}
        try { _temp = Double.parseDouble(p.getProperty("simulate.temperature", "22.1")); } catch (Exception e) {}
        try { _hum = Double.parseDouble(p.getProperty("simulate.humidity", "48.2")); } catch (Exception e) {}
    }
    
    private boolean _vhw   = true;  // water spead and heading
    private boolean _gll   = false;  // gps
    private boolean _rmc   = false;  // gps
    private boolean _dpt   = true;  // depth
    private boolean _dbt   = true;  // depth
    private boolean _mtw   = true;  // water temp
    private boolean _mta   = false;  // air temp
    private boolean _mbb   = false;  // atm pressure
    private boolean _mhu   = false;  // humidity
    private boolean _mwv_a = true;  // wind apparent
    private boolean _mwv_t = true;  // wind true
    private boolean _vwr   = true;  // relative wind speed and angle (apparent)
	private boolean _hdm   = true; // magn heading
    private boolean _hdg   = true;  // magn heading + variation/deviation
    private boolean _hdt   = true; // true heading
    private boolean _xxx   = false;  // sensors
    private boolean _vtg   = true;  // cog-sog
    private boolean _xdrDiag = true;
    private boolean _xdrMeteo = true;
    private boolean _xdrMeteoAtm = true;
    private boolean _xdrMeteoHum = true;
    private boolean _xdrMeteoTmp = true;
    private boolean _xdrGYR = true;
    
    private double _speed = 5.9;
    private double _wSpeed = 11;
    private double _wDirection = 34;
	private double _heading = 354;
	private double _temp = 29.5;
    private double _press = 1030;
    private double _hum = 48.1;
    
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
						loadConf();
						
						double ph = System.currentTimeMillis() / (1000d*60d*15d) * 2 * Math.PI; // 15 minutes phase
						double depth = 10.0 + Math.sin(ph)*5.0;

						//double rh = getRec_heading();
						//double heading = Double.isNaN(rh)?(_heading + r.nextDouble() * 3.0):rh;
						double heading = _heading + r.nextDouble() * 3.0;
						
						double speed = _speed + r.nextDouble() * 0.5;
						double wSpeed = _wSpeed + Math.sin(i/10.0 * Math.PI);
						double wDirection = _wDirection + r.nextDouble() * 3.0;
						TrueWind trueWind = new TrueWind(speed, wDirection, wSpeed);
						
						double temp = _temp + (new Random().nextDouble()/10.0);
						double press = _press + (new Random().nextDouble()/10.0);
						
                        double a = i;
                        double xm = Math.cos(a) * 150 + r.nextDouble() * 5 + 75; 
                        double ym = Math.sin(a) * 150 + r.nextDouble() * 5 + 45;
                        double zm = 0;
                        double roll = (new Random().nextDouble()*5) + 40.0*(Math.min(wSpeed, 15.0)/15.0) ;
                        double pitch = (new Random().nextDouble()*5) + 0;
						
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
                            //hdg.setDeviation(0.0);
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
                            mta.setTemperature(temp);;
                            NMEASimulatorSource.this.notify(mta);
                        }
                        
                        if (_mbb) {
                            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(id, "MMB");
                            mmb.setBars(press/1000.0);
                            NMEASimulatorSource.this.notify(mmb);
                        }
                        
                        if (_mhu) {
                            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(id, "MHU");
                            mhu.setRelativeHumidity(_hum);
                            NMEASimulatorSource.this.notify(mhu);
                        }
                        
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

	                        if (_xdrMeteoAtm) xdr.addMeasurement(new Measurement("P", press, "B", "Barometer_0"));
	                        if (_xdrMeteoTmp) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp_0"));
	                        if (_xdrMeteoHum) xdr.addMeasurement(new Measurement("C", _hum, "H", "Humidity_0"));
	                        if (_xdrMeteoAtm) xdr.addMeasurement(new Measurement("P", press, "B", "Barometer_1"));
	                        if (_xdrMeteoTmp) xdr.addMeasurement(new Measurement("C", temp, "C", "AirTemp_1"));
	                        if (_xdrMeteoHum) xdr.addMeasurement(new Measurement("C", _hum, "H", "Humidity_1"));
                            NMEASimulatorSource.this.notify(xdr);
                        }
                        
                        if (_xdrDiag) {
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
		return _speed;
	}

	public void setSpeed(double speed) {
		this._speed = speed;
	}

	
}
