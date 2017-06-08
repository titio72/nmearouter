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
    
    private static final int TEMP = 0; 
    private static final int W_TEMP = 1; 
    private static final int PRESS = 2; 
    private static final int WIND = 3; 
    private static final int WIND_D = 4; 
    private static final int HUM = 5; 
    private Sample[] samples = new Sample[6];
    private static final String[] TYPES = new String[] 		{"AT0", "WT_", 	"PR_", 	"TW_", 	"TWD", 	"HUM"};
    private static final String[] TYPES_D = new String[] 	{"V", 	"V", 	"V", 	"V", 	"A", 	"V"};
    
    private class Sample {
        double Avg = Double.NaN;
        double Max = Double.NaN;
        double Min = Double.NaN;
        int Samples = 0;
        
        void add(double v, String type) {
            if (Samples == 0) {
            	v = Utils.normalizeDegrees0_360(v);
                Avg = v;
                Max = v;
                Min = v;
                Samples = 1;
            } else {
            	if ("A".equals(type)) {
					double a = Utils.getNormal(Avg, v);
	                Avg = ((Avg * Samples) +  a) / (Samples +1);
	                Avg = Utils.normalizeDegrees0_360(Avg);
	                Max = Utils.normalizeDegrees0_360(Math.max(Max,  a));
	                Min = Utils.normalizeDegrees0_360(Math.min(Min,  a));
            	} else {
	                Avg = ((Avg * Samples) +  v) / (Samples +1);
	                Max = Math.max(Max,  v);
	                Min = Math.min(Min,  v);
            	}
                Samples++;
            }
        }
    }
    
    private DBHelper db;
    private PreparedStatement stm;

    public NMEAMeteoTarget(NMEACache cache, NMEAStream stream, String name, QOS qos) {
        super(cache, stream, name, qos);
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
        synchronized (samples) {
        	long ts = System.currentTimeMillis() - SAMPLING;
            for (int i = 0; i<samples.length; i++) {
                if (samples[i]!=null) {
                    write(TYPES[i], samples[i], ts);
                    samples[i] = null;
                }
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

	private void collect(int type, double d) {
        synchronized (samples) {
            if (samples[type]==null) samples[type] = new Sample();
            samples[type].add(d, TYPES_D[type]);
        }
    }

    private void write(String type, Sample v, long ts) {
        try {
            if (v.Samples>0) {
                stm.setString(1, type);
                stm.setDouble(2, v.Avg);
                stm.setDouble(3, v.Max);
                stm.setDouble(4, v.Min);
                stm.setTimestamp(5, new Timestamp(ts));
                stm.execute();
            }
        } catch (Exception e) {
            getLogger().Error("Cannot write meteo info type {" + type + "} Value {" + v +"} Agent {" + getName() + "}", e);
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
