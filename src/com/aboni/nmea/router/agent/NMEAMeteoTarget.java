package com.aboni.nmea.router.agent;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Timer;
import java.util.TimerTask;

import com.aboni.geo.Utils;
import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
import com.aboni.nmea.router.impl.NMEAAgentImpl;
import com.aboni.utils.DBHelper;

import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAMeteoTarget extends NMEAAgentImpl {

    private static final long SAMPLING = 60000;

	public enum Type {
		Scalar,
		Angle
	}
    
    public class Serie {
    	private int id;
    	private String tag;
    	private Type type;
        private double avg;
        private double max;
        private double min;
        int samples = 0;
        
    	public String getTag() { return tag; }
    	public Type getType() { return type; }
    	public int getId() { return id; }
    	public int getSamples() { return samples; }
    	public double getAvg() { return avg; }
    	public double getMin() { return min; }
    	public double getMax() { return max; }
    	
    	private Serie(int id, String tag, Type type) {
    		this.id = id;
    		this.tag = tag;
    		this.type = type;
    		reset();
    	}
    	
    	public void reset() {
            avg = Double.NaN;
            max = Double.NaN;
            min = Double.NaN;
            samples = 0;
    	}
    	
    	public void add(double v) {
            if (samples == 0) {
            	v = Utils.normalizeDegrees0_360(v);
                avg = v;
                max = v;
                min = v;
                samples = 1;
            } else {
            	if (Type.Angle.equals(type)) {
					double a = Utils.getNormal(avg, v);
	                avg = ((avg * samples) +  a) / (samples +1);
	                avg = Utils.normalizeDegrees0_360(avg);
	                max = Utils.normalizeDegrees0_360(Math.max(max,  a));
	                min = Utils.normalizeDegrees0_360(Math.min(min,  a));
            	} else {
	                avg = ((avg * samples) +  v) / (samples +1);
	                max = Math.max(max,  v);
	                min = Math.min(min,  v);
            	}
                samples++;
            }
        }
    }
    
    private static final int TEMP = 0; 
    private static final int W_TEMP = 1; 
    private static final int PRESS = 2; 
    private static final int WIND = 3; 
    private static final int WIND_D = 4; 
    private static final int HUM = 5; 

    private Serie[] series = new Serie[] {
    		new Serie(TEMP, "AT0", Type.Scalar),
    		new Serie(TEMP, "WT_", Type.Scalar),
    		new Serie(TEMP, "PR_", Type.Scalar),
    		new Serie(TEMP, "TW_", Type.Scalar),
    		new Serie(TEMP, "TWD", Type.Angle),
    		new Serie(TEMP, "HUM", Type.Scalar)
    };
    
    private DBHelper db;
    private PreparedStatement stm;
    private NMEACache cache;

    public NMEAMeteoTarget(NMEACache cache, NMEAStream stream, String name, QOS qos) {
        super(cache, stream, name, qos);
        this.cache = cache;
        setSourceTarget(false, true);
    }

    @Override
	public String getDescription() {
		return "";
	}
	

    @Override
    protected boolean onActivate() {
        try {
            db = new DBHelper(true);
            stm = db.getConnection().prepareStatement("insert into meteo (type, v, vMax, vMin, TS) values (?, ?, ?, ?, ?)");
            
            new Timer(true).scheduleAtFixedRate(new TimerTask() {
                
                @Override
                public void run() {
                    dumpStats();
                }
            }, 0, SAMPLING);
            
            return true;
        } catch (Exception e) {
            getLogger().Error("Error connecting db Agent {Meteo}", e);
            return false;
        }
    }
    
    protected void dumpStats() {
        synchronized (series) {
        	long ts = System.currentTimeMillis() - SAMPLING;
            for (int i = 0; i<series.length; i++) {
                write(series[i], ts);
                series[i].reset();
            }
        }
    }

    @Override
    protected void onDeactivate() {
        if (isStarted()) {
            try {
                db.close();
            } catch (Exception e) {}
        }
    }    
    
    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    	if (cache.isTimeSynced()) {
	        if (s instanceof net.sf.marineapi.nmea.sentence.MTASentence) {
	            processTemp((MTASentence)s);
	        } else if (s instanceof MMBSentence) {
	            processPressure((MMBSentence)s);
	        } else if (s instanceof MTWSentence) {
	            processWaterTemp((MTWSentence)s);
	        } else if (s instanceof MHUSentence) {
	            processHumidity((MHUSentence)s);
	        } else if (s instanceof MWDSentence) {
	            processWind((MWDSentence)s);
	        }
    	}
    }

	private void collect(int id, double d) {
        synchronized (series) {
        	Serie s = series[id];
            s.add(d);
        }
    }

    private void write(Serie s,  long ts) {
    	if (s!=null) {
    		try {
	            if (s.getSamples()>0) {
	                stm.setString(1, s.getTag());
	                stm.setDouble(2, s.getAvg());
	                stm.setDouble(3, s.getMax());
	                stm.setDouble(4, s.getMin());
	                stm.setTimestamp(5, new Timestamp(ts));
	                stm.execute();
	            }
	        } catch (Exception e) {
	            getLogger().Error("Cannot write meteo info type {" + s.getType() + "} Value {" + s.getAvg() +"} Agent {" + getName() + "}", e);
	        }
    	}
    }

    private void processWind(MWDSentence s) {
    	if (Double.isNaN(s.getWindSpeedKnots())) {
    		collect(WIND, s.getWindSpeed() * 1.94384);
    	} else {
    		collect(WIND, s.getWindSpeedKnots());
    	}
        collect(WIND_D, s.getMagneticWindDirection());
    }

    private void processWaterTemp(MTWSentence s) {
        collect(W_TEMP, s.getTemperature());
    }

    private void processPressure(MMBSentence s) {
        collect(PRESS, s.getBars() * 1000);
    }

    private void processTemp(MTASentence s) {
        collect(TEMP, s.getTemperature());
    }

    private void processHumidity(MHUSentence s) {
        collect(HUM, s.getAbsoluteHumidity());
    }
}
