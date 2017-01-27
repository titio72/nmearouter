package com.aboni.nmea.router;

import java.io.IOException;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.log.Log;

import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEASentenceListener;
import com.aboni.nmea.router.agent.NMEASourceSensor;
import com.aboni.nmea.router.services.WebInterface;
import com.aboni.nmea.sentences.NMEAUtils;
import com.aboni.nmea.sentences.XXXPParser;
import com.aboni.nmea.sentences.XXXPSentence;
import com.aboni.sensors.DoCalibration;
import com.aboni.utils.ServerLog;

import net.sf.marineapi.nmea.sentence.Sentence;

public class StartRouter {

    private static final String CALIBRATION = "-cal";
    private static final String PLAY = "-play";
    private static final String SENSOR = "-sensor";
    private static final String WEB = "-web";
    private static final String HELP = "-help";
    
	private static int checkFlag(String flag, String[] args) {
        if (args!=null) {
            for (int i = 0; i<args.length; i++) {
                if (flag.equals(args[i])) return i;
            }
        }
        return -1;
	}
    
	public static void main(String[] args) {
		int ix;
        if (checkFlag(HELP, args)>=0) {
            System.out.println("-web : activate web interface\r\n" + 
                    "-sensor : sensor monitor\r\n" +
                    "-play : NMEA file to play\r\n" +
                    "-cal : compass calibration\r\n");
        } else if ((ix = checkFlag(PLAY, args))>=0) {
        	startRouter(args, new NMEARouterPlayerBuilderImpl(args[ix + 1]));
        } else if (checkFlag(SENSOR, args)>=0) {
            startSensors(args);
        } else if (checkFlag(CALIBRATION, args)>=0) {
            startCalibration(args);
	    } else {
            startRouter(args, new NMEARouterDefaultBuilderImpl("router.xml"));
            startWebInterface(args);
	    }
	}

	private static void startWebInterface(String[] args) {
        if (checkFlag(WEB, args)>=0) {
            try {
                org.eclipse.jetty.util.log.Logger l;
                l = new org.eclipse.jetty.util.log.JavaUtilLog("jetty");
                
                Log.setLog(l); 
                
                Server server = new Server(1112);
             
                ResourceHandler resource_handler = new ResourceHandler();
                //resource_handler.setDirectoriesListed(true);
                resource_handler.setWelcomeFiles(new String[]{ "index.html" });
                resource_handler.setResourceBase("./web");
         
                HandlerList handlers = new HandlerList();
                handlers.setHandlers(new Handler[] { resource_handler, new WebInterface() });
                server.setHandler(handlers);
                server.start();
                server.join();
            } catch (Exception e) {
                e.printStackTrace();
            }
	    }
    }

    private static void startCalibration(String[] args) {
        DoCalibration.main(args);
    }

    private static void startSensors(String args[]) {
        System.out.println("Start");
        NMEAUtils.registerExtraSentences();
        NMEASourceSensor s = new NMEASourceSensor("test", null);
        s.setSentenceListener(new NMEASentenceListener() {
            
            @Override
            public void onSentence(Sentence s, NMEAAgent src) {
                if (s.getSentenceId().equals(XXXPParser.NMEA_SENTENCE_TYPE)) {
                    XXXPSentence x = (XXXPSentence)s;
                    
                    double h, t, p, v0, v1, rpm, roll, pitch;
                    try { h = x.getHeading(); } catch (Exception e) { h = 0.0; }
                    try { t = x.getTemperature(); } catch (Exception e) { t = 0.0; }
                    try { p = x.getPressure(); } catch (Exception e) { p = 0.0; }
                    try { roll = x.getRotationX(); } catch (Exception e) { roll = 0.0; }
                    try { pitch = x.getRotationY(); } catch (Exception e) { pitch = 0.0; }
                    try { v0 = x.getVoltage(); } catch (Exception e) { v0 = 0.0; }
                    try { v1 = x.getVoltage1(); } catch (Exception e) { v1 = 0.0; }
                    try { rpm = x.getRPM(); } catch (Exception e) { rpm = 0.0; }
                    
                    System.out.format("\rH: %-7.2f R: %-7.2f Pi: %-7.2f T: %-6.2f P: %6.2f V: %6.2f %6.2f RPM: %4.0f" , h, roll, pitch, t, p, v0, v1, rpm);   
                }
            }
        });
        s.start();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void startRouter(String[] args, NMEARouterBuilder builder) {
        System.out.println("Start");
        NMEAUtils.registerExtraSentences();
        NMEAStreamProvider.getStreamInstance(); // be sure the stream started
    	if (builder.init()!=null) {
    		NMEARouter r = builder.getRouter();
    		switch (r.getPreferredLogLevelType()) {
	    		case DEBUG: 
	            	ServerLog.getLogger().setDebug(); break;
	    		case WARNING: 
	            	ServerLog.getLogger().setWarning(); break;
	    		case ERROR: 
	            	ServerLog.getLogger().setError(); break;
	    		case NONE: 
	            	ServerLog.getLogger().setNone(); break;
            	default:
	            	ServerLog.getLogger().setInfo(); break;
    		}
        	NMEARouterProvider.setRouter(r);
        	r.start();
    	}
    }
}
