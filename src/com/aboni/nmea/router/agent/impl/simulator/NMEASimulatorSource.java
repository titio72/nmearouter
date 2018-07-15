package com.aboni.nmea.router.agent.impl.simulator;

import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import com.aboni.geo.ApparentWind;
import com.aboni.geo.NavSimulator;
import com.aboni.misc.PolarTable;
import com.aboni.misc.Utils;
import com.aboni.utils.Constants;
import com.aboni.utils.ServerLog;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.nmea.sentences.VWRSentence;
import com.aboni.seatalk.Stalk84;

import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.DBTSentence;
import net.sf.marineapi.nmea.sentence.DPTSentence;
import net.sf.marineapi.nmea.sentence.GLLSentence;
import net.sf.marineapi.nmea.sentence.HDGSentence;
import net.sf.marineapi.nmea.sentence.HDMSentence;
import net.sf.marineapi.nmea.sentence.HDTSentence;
import net.sf.marineapi.nmea.sentence.MDASentence;
import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.MWVSentence;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.sentence.VHWSentence;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.sentence.VWTSentence;
import net.sf.marineapi.nmea.sentence.XDRSentence;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.DataStatus;
import net.sf.marineapi.nmea.util.Date;
import net.sf.marineapi.nmea.util.Direction;
import net.sf.marineapi.nmea.util.FaaMode;
import net.sf.marineapi.nmea.util.Measurement;
import net.sf.marineapi.nmea.util.Position;
import net.sf.marineapi.nmea.util.Side;
import net.sf.marineapi.nmea.util.Time;
import net.sf.marineapi.nmea.util.Units;

public class NMEASimulatorSource extends NMEAAgentImpl {

	private int headingAuto = Integer.MIN_VALUE;
	private double refHeading = Double.NaN;
	private PolarTable polars;
	
	public static NMEASimulatorSource SIMULATOR;
    private NMEASimulatorSourceSettings data = new NMEASimulatorSourceSettings();

	public NMEASimulatorSource(NMEACache cache, NMEAStream stream, String name) {
		this(cache, stream, name, null);
	}
	
	public NMEASimulatorSource(NMEACache cache, NMEAStream stream, String name, QOS qos) {
		super(cache, stream, name, qos);
        setSourceTarget(true, true);
        if (SIMULATOR!=null) throw new RuntimeException();
        else SIMULATOR = this;
        
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
        	if (data._polars!=null) {
        		if (!data._polars.equals(lastPolarFile)) {
	        		File f = new File(Constants.CONF_DIR, data._polars);
	        		polars.load(new FileReader(f));
	        		lastPolarFile = data._polars;
        		}
        	}
		} catch (Exception e) {
			getLogger().Error("Cannot load polars", e);
		}		
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
		return refHeading;
	}

    public void setHeading(double _heading) {
		this.refHeading = _heading;
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
						loadPolars();
					
						if (Double.isNaN(refHeading)) refHeading = data._heading;
						
						long newTS = System.currentTimeMillis();
						double ph15m = System.currentTimeMillis() / (1000d*60d*15d) * 2 * Math.PI; // 15 minutes phase
						//double ph1h = System.currentTimeMillis() / (1000d*60d*60d*1d) * 2 * Math.PI; // 1h phase
						double depth = round(data._depth + Math.sin(ph15m) * data._depthRange, 1);
						double hdg = Utils.normalizeDegrees0_360(refHeading + r.nextDouble() * 3.0);
						
						double absoluteWindSpeed = data._wSpeed + r.nextDouble() * 1.0; 
						double absoluteWindDir = data._wDirection + r.nextDouble() * 2.0; 

						double tWSpeed = 		absoluteWindSpeed;
						double tWDirection = 	Utils.normalizeDegrees0_360(absoluteWindDir - hdg);

						double speed;
						if (data._usePolars) {
							speed = polars.getSpeed((int)tWDirection, (float)tWSpeed) * data._polarCoeff;
						} else {
							speed = round(data._speed * (1.0 + r.nextDouble()/10.0), 1);
						}

						ApparentWind aWind = new ApparentWind(speed, tWDirection, tWSpeed);
						double aWSpeed = 		aWind.getApparentWindSpeed();
						double aWDirection = 	Utils.normalizeDegrees0_360(aWind.getApparentWindDeg());
						
						double temp = round(data._temp + (new Random().nextDouble()/10.0), 2);
						double press = round(data._press + (new Random().nextDouble()/10.0), 1);
                        double roll = round(new Random().nextDouble()*5, 1);
                        double pitch = round((new Random().nextDouble()*5) + 0, 1);

                        if (lastTS!=0) {
                        	double dTime = (double)(newTS-lastTS) / 1000d / 60d / 60d;
                        	pos = NavSimulator.calcNewLL(pos, hdg, speed * dTime);
                        }
						lastTS = newTS;
						
						if (data._vhw) {
    						VHWSentence s = (VHWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VHW);
                            s.setHeading(hdg);
                            s.setMagneticHeading(hdg);
    						s.setSpeedKnots(speed);
    						s.setSpeedKmh(speed * 1.852);
    						NMEASimulatorSource.this.notify(s);
						}
						
						if (data._gll) {
    						GLLSentence s1 = (GLLSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.GLL);
    						s1.setPosition(pos);
    						s1.setStatus(DataStatus.ACTIVE);
    						s1.setTime(new Time());
    						NMEASimulatorSource.this.notify(s1);
						}
						
						if (data._rmc) {
    						RMCSentence rmc = (RMCSentence)SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
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
    						rmc.setPosition(pos);
    						NMEASimulatorSource.this.notify(rmc);
						}

						if (data._dpt) {
    						DPTSentence d = (DPTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DPT);
    						d.setDepth(depth); 
    						d.setOffset(data._depthOffset); 
    						NMEASimulatorSource.this.notify(d);
						}
						
						if (data._dbt) {
    						DBTSentence d = (DBTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.DBT);
    						d.setDepth(depth);
    						NMEASimulatorSource.this.notify(d);
						}
						
						if (data._mtw) {
    						MTWSentence t = (MTWSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MTW);
    						t.setTemperature(28.5);
    						NMEASimulatorSource.this.notify(t);
						}
						
						if (data._mwv_a) {
                            MWVSentence v = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
                            v.setSpeedUnit(Units.KNOT);
                            v.setAngle(aWDirection);
                            v.setSpeed(aWSpeed);
                            v.setTrue(false);
                            v.setStatus(DataStatus.ACTIVE);
                            NMEASimulatorSource.this.notify(v);
						}
                        
						if (data._mwv_t) {
                            MWVSentence vt = (MWVSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.MWV);
                            vt.setSpeedUnit(Units.KNOT);
                            vt.setAngle(tWDirection);
                            vt.setSpeed(tWSpeed);
                            vt.setTrue(true);
                            vt.setStatus(DataStatus.ACTIVE);
                            NMEASimulatorSource.this.notify(vt);
						}
                        
						if (data._vwr) {
                            VWRSentence vwr = (VWRSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWR");
                            vwr.setAngle(aWDirection>180?360-aWDirection:aWDirection);
                            vwr.setSpeed(aWSpeed);
                            vwr.setSide(Side.PORT);
                            vwr.setStatus(DataStatus.ACTIVE);
                            NMEASimulatorSource.this.notify(vwr);
						}
                        
						if (data._vwr) {
                            VWTSentence vwt = (VWTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "VWT");
                            vwt.setWindAngle(tWDirection>180?360-tWDirection:tWDirection);
                            vwt.setSpeedKnots(tWSpeed);
                            vwt.setDirectionLeftRight(tWDirection>180?Direction.LEFT:Direction.RIGHT);
                            NMEASimulatorSource.this.notify(vwt);
						}
                        
                        if (data._hdm) {
                            HDMSentence hdm = (HDMSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDM);
                            hdm.setHeading(hdg);
                            NMEASimulatorSource.this.notify(hdm);
                        }
                        
                        if (data._hdt) {
                            HDTSentence hdt = (HDTSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDT);
                            hdt.setHeading(hdg);
                            NMEASimulatorSource.this.notify(hdt);
                        }
                        
                        if (data._hdg) {
                            HDGSentence hdgS = (HDGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.HDG);
                            hdgS.setHeading(hdg);
                            //hdg.setDeviation(0.0);
                            NMEASimulatorSource.this.notify(hdgS);
                        }
                        
                        if (data._vtg) {
                            VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.VTG);
                            vtg.setMagneticCourse(hdg);
                            vtg.setTrueCourse(hdg);
                            vtg.setMode(FaaMode.AUTOMATIC);
                            vtg.setSpeedKnots(speed);
                            vtg.setSpeedKmh(speed * 1.852);
                            NMEASimulatorSource.this.notify(vtg);
                        }
                        
                        if (data._mta) {
                            MTASentence mta = (MTASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MTA");
                            mta.setTemperature(temp);;
                            NMEASimulatorSource.this.notify(mta);
                        }
                        
                        if (data._mbb) {
                            MMBSentence mmb = (MMBSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MMB");
                            mmb.setBars(press/1000.0);
                            NMEASimulatorSource.this.notify(mmb);
                        }
                        
                        if (data._mhu) {
                            MHUSentence mhu = (MHUSentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MHU");
                            mhu.setRelativeHumidity(data._hum);
                            NMEASimulatorSource.this.notify(mhu);
                        }
                        
                        if (data._mda) {
                            MDASentence mda = (MDASentence) SentenceFactory.getInstance().createParser(TalkerId.II, "MDA");
                            mda.setRelativeHumidity(data._hum);
                            mda.setAirTemperature(temp);
                            mda.setPrimaryBarometricPressure(press/1000.0);
                            mda.setPrimaryBarometricPressureUnit('B');
                            mda.setWaterTemperature(28.5);
                            mda.setMagneticWindDirection(tWDirection + hdg);
                            mda.setTrueWindDirection(tWDirection + hdg);
                            mda.setWindSpeedKnots(tWSpeed);
                            NMEASimulatorSource.this.notify(mda);
                        }
                        
                        if (data._xdrGYR) {
	                        XDRSentence xdr = (XDRSentence)SentenceFactory.getInstance().createParser(TalkerId.II, SentenceId.XDR.toString());
	                        xdr.addMeasurement(new Measurement("A", hdg, "D", "HEAD"));
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
                        
                        if (data._autoPilot) {
                        	sendAutopilotStatus();
                        }
					} catch (InterruptedException e) {
						ServerLog.getLogger().Error("Error simulating", e);
						e.printStackTrace();
					}
				}
				
			}


		}).start();

			
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
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    	if (!source.equals(this) && s instanceof STALKSentence) {
    		STALKSentence t = (STALKSentence)s;
    		if (t.getCommand().equals("86")) {
    			String[] p = t.getParameters();
    			if (p[0].equals("21")) {
    				if (p[1].equals("01") && p[2].equals("FE")) {
    					if (headingAuto==Integer.MIN_VALUE) {
    						headingAuto = (int)refHeading;
    						sendAutopilotStatus();
    					}
    				} else if (p[1].equals("02") && p[2].equals("FD")) {
    					if (headingAuto!=Integer.MIN_VALUE) {
    						headingAuto = Integer.MIN_VALUE;
    						sendAutopilotStatus();
    					}
    				}
    			} else if (p[0].equals("11")) {
    				if (p[1].equals("05") && p[2].equals("FA")) {
    					if (headingAuto!=Integer.MIN_VALUE) {
    						headingAuto -= 1;
    						sendAutopilotStatus();
    					}
						refHeading -= 1;
    				}
    				else if (p[1].equals("06") && p[2].equals("F9")) {
    					if (headingAuto!=Integer.MIN_VALUE) {
    						headingAuto -= 10;
    						sendAutopilotStatus();
    					}
						refHeading -= 10;
    				}
    				else if (p[1].equals("07") && p[2].equals("F8")) {
    					if (headingAuto!=Integer.MIN_VALUE) {
    						headingAuto += 1;
    						sendAutopilotStatus();
    					}
						refHeading += 1;
    				}
    				else if (p[1].equals("08") && p[2].equals("F7")) {
    					if (headingAuto!=Integer.MIN_VALUE) {
    						headingAuto += 10;
    						sendAutopilotStatus();
    					}
						refHeading += 10;
    				}
    			}
    		}
    			
    	}
    		
    }

	public double getSpeed() {
		return data._speed;
	}

	public void setSpeed(double speed) {
		this.data._speed = speed;
	}
}
