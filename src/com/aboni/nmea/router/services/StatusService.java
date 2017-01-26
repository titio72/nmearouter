package com.aboni.nmea.router.services;

import java.io.PrintWriter;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEARouterProvider;
import com.aboni.utils.DataEvent;

import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.util.Measurement;

public class StatusService implements WebService {

	private boolean json;
		
    public StatusService() {
        this(false);
    }
    
    public StatusService(boolean json) {
		this.json = json;
	}

    private String getContentType() {
        return json?"application/json":"text/xml;charset=utf-8";
    }
    
    private void writeAttribute(PrintWriter w, DataEvent<Measurement> s, String attribute) {
        try {
            Object res = s.data.getValue();
            if (json) {
                if (res instanceof Number)
                    w.format("\"%s\":%.2f",  attribute, ((Double)res).doubleValue());
                else 
                    w.format("\"%s\":\"%s\"",  attribute, res.toString());
            } else {
                if (res instanceof Number)
                    w.format("<Value name='%s'>%.2f</Value>",  attribute, ((Double)res).doubleValue());
                else 
                    w.format("<Value name='%s'>%s</Value>",  attribute, res.toString());
            }
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }
    
    
	/* (non-Javadoc)
	 * @see com.aboni.nmea.router.services.WebService#doIt(com.aboni.nmea.router.services.ServiceConfig, com.aboni.nmea.router.services.ServiceOutput)
	 */
	@Override
	public void doIt(ServiceConfig config, ServiceOutput response) {
        json = "json".equals(config.getParameter("format"));
        response.setContentType(getContentType());
        try {
            if (json) {
                response.getWriter().print("{");
            } else {
                response.getWriter().println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                response.getWriter().println("<RouterStatus>");
            }
        
            response.getWriter().print(json?"\"sensor\":{":"<Sensor>\n");

            NMEACache c = NMEARouterProvider.getRouter().getCache();
            int counter = 0;
            for (String sensor: c.getSensors()) {
            	if (counter>0) response.getWriter().print(",");
            	counter++;
                writeAttribute(response.getWriter(), c.getSensorData(sensor), sensor);
            }
            response.getWriter().print(json?"}":"</Sensor>\n");

            if (c.getLastPosition()!=null) {
            	PositionSentence lastPos = c.getLastPosition().data;
            	if (lastPos!=null && json) response.getWriter().print(",");
                response.getWriter().print(json?
                        ",\"Position\":{" 
                        :"<Position>\n");
                try {
                    if (json) {
                        response.getWriter().format("\"Latitude\":\"%.6f %s\",",
                                lastPos.getPosition().getLatitude(), lastPos.getPosition().getLatitudeHemisphere());
                        response.getWriter().format("\"Longitude\":\"%.6f %s\"",
                                lastPos.getPosition().getLongitude(), lastPos.getPosition().getLongitudeHemisphere());
                    } else {
                        
                        response.getWriter().format("<Latitude>%.6f %s</Latitude>",
                                lastPos.getPosition().getLatitude(), lastPos.getPosition().getLatitudeHemisphere());
                        response.getWriter().format("<Longitude>%.6f %s</Longitude>",
                                lastPos.getPosition().getLongitude(), lastPos.getPosition().getLongitudeHemisphere());
                    }
                } catch (Exception e) {
                    
                }
                response.getWriter().print(json?"}":"</Position>\n");
            }
            
            if (json) {
                response.getWriter().print("}");
            } else {
                response.getWriter().println("</RouterStatus>");
            }
            response.ok();

        } catch (Exception e) {
            response.setContentType("text/html;charset=utf-8");
            try { e.printStackTrace(response.getWriter()); } catch (Exception ee) {}
            response.error(e.getMessage());
        }

	}
	
}
