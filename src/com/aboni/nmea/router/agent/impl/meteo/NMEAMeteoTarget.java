package com.aboni.nmea.router.agent.impl.meteo;

import java.util.Timer;
import java.util.TimerTask;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEAStream;
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

	private StatsWriter writer;
	
    private static final long SAMPLING = 60000;

    private static final int TEMP = 0; 
    private static final int W_TEMP = 1; 
    private static final int PRESS = 2; 
    private static final int WIND = 3; 
    private static final int WIND_D = 4; 
    private static final int HUM = 5; 

    private Serie[] series = new Serie[] {
    		new ScalarSerie(TEMP, "AT0", -20.0, 50.0),
    		new ScalarSerie(W_TEMP, "WT_", -20.0, 50.0),
    		new ScalarSerie(PRESS, "PR_", 800.0, 1100.0),
    		new ScalarSerie(WIND, "TW_", 0.0, 100.0),
    		new AngleSerie(WIND_D, "TWD"),
    		new ScalarSerie(HUM, "HUM", 0.0, 150.0)
    };
    
    private NMEACache cache;

    public NMEAMeteoTarget(NMEACache cache, NMEAStream stream, String name, QOS qos, StatsWriter w) {
        super(cache, stream, name, qos);
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
		        } else {
		        	//System.out.println("ss");
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
