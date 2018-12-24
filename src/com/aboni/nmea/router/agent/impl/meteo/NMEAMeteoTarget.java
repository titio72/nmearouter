package com.aboni.nmea.router.agent.impl.meteo;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.agent.impl.NMEAAgentImpl;
import com.aboni.utils.Serie;
import com.aboni.utils.ScalarSerie;
import com.aboni.utils.AngleSerie;

import net.sf.marineapi.nmea.sentence.MHUSentence;
import net.sf.marineapi.nmea.sentence.MMBSentence;
import net.sf.marineapi.nmea.sentence.MTASentence;
import net.sf.marineapi.nmea.sentence.MTWSentence;
import net.sf.marineapi.nmea.sentence.MWDSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public class NMEAMeteoTarget extends NMEAAgentImpl {

	private final StatsWriter writer;
	
    private static final int SAMPLING_FACTOR = 60; // every 60 timers dumps
    private int timerCount;

    private static final int TEMP = 0; 
    private static final int W_TEMP = 1; 
    private static final int PRESS = 2; 
    private static final int WIND = 3; 
    private static final int WIND_D = 4; 
    private static final int HUM = 5; 

    private final Serie[] series = new Serie[] {
    		new ScalarSerie("AT0", -20.0, 50.0),
    		new ScalarSerie("WT_", -20.0, 50.0),
    		new ScalarSerie("PR_", 800.0, 1100.0),
    		new ScalarSerie("TW_", 0.0, 100.0),
    		new AngleSerie("TWD"),
    		new ScalarSerie("HUM", 0.0, 150.0)
    };
    
    private final NMEACache cache;

    public NMEAMeteoTarget(NMEACache cache, String name, QOS qos, StatsWriter w) {
        super(cache, name, qos);
        this.cache = cache;
        setSourceTarget(false, true);
    	writer = w;
    }

    @Override
	public String getDescription() {
		return "Meteo data sampling";
	}

    @Override
    protected boolean onActivate() {
        try {
            if (writer!=null) writer.init();
            return true;
        } catch (Exception e) {
            getLogger().Error("Error connecting db Agent {Meteo}", e);
            return false;
        }
    }
    
    
    
    @Override
    public void onTimer() {
    	timerCount = (timerCount+1) % SAMPLING_FACTOR;
    	if (timerCount==0) dumpStats();
    	super.onTimer();
    }
    
    
    protected void dumpStats() {
        synchronized (series) {
        	long ts = System.currentTimeMillis();
            for (Serie series1 : series) {
                write(series1, ts);
                series1.reset();
            }
        }
    }

    @Override
    protected void onDeactivate() {
        if (isStarted() && writer!=null) {
        	writer.dispose();
        }
    }    
    
    @Override
    protected void doWithSentence(Sentence s, NMEAAgent source) {
    	try {
	    	if (cache.isTimeSynced()) {
		        if (s instanceof MTASentence) {
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
    	} catch (Exception e) {
    		getLogger().Warning("Error processing meteo stats {" + s + "} erro {" + e + "}");
    	}
    }

	private void collect(int id, double d) {
        synchronized (series) {
        	Serie s = series[id];
            s.add(d);
        }
    }

    private void write(Serie s,  long ts) {
    	if (writer!=null && s!=null && s.getSamples()>0) {
        	writer.write(s, ts);
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
        collect(HUM, s.getRelativeHumidity());
    }
}
