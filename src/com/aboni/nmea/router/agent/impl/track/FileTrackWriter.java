package com.aboni.nmea.router.agent.impl.track;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.ServerLog;

public class FileTrackWriter implements TrackWriter {

    private DecimalFormat myPosFormatter = new DecimalFormat("000.0000000");
    private DecimalFormat mySpeedFormatter = new DecimalFormat("#0.0");
    private DecimalFormat myDistFormatter = new DecimalFormat("#0.00000");
    private SimpleDateFormat tsFormatter;
    
    private String fileName;
    
    public FileTrackWriter(String file) {
        tsFormatter = new SimpleDateFormat("ddMMyy HHmmss.SSS");
        tsFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        fileName = file;
    }
    
    @Override
    public void write(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
        String msg = getPositionString(p, anchor, dist, speed, maxSpeed, interval);
        if (msg!=null) {
            writeLine(msg);
        }
    }

    private void writeLine(String line) {
        try {
            if (fileName!=null) {
                Files.write(Paths.get(fileName), line.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (Exception e) {
            ServerLog.getLogger().Error("Cannot write position log", e);
        }
    }

    private String getPositionString(GeoPositionT pos, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
    	if (pos!=null) {
	        String msg = 
	        		pos.getTimestamp() +
	        		" " + myPosFormatter.format(Math.abs(pos.getLatitude())) + ((pos.getLatitude()>0)?"N":"S") + 
	                " " + myPosFormatter.format(Math.abs(pos.getLongitude())) + ((pos.getLongitude()>0)?"E":"W") +
	                " " + (anchor?"A":"T") + 
	                " " + myDistFormatter.format(dist) + 
	                " " + mySpeedFormatter.format(speed) + 
	                " " + mySpeedFormatter.format(maxSpeed) +
	                " " + interval +
	                "\r\n"; 
	        return msg;
    	} else {
    		return null;
    	}
    }
    
    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void dispose() {
    }
}
