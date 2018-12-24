package com.aboni.nmea.router.agent.impl.track;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.ServerLog;

public class FileTrackWriter implements TrackWriter {

    private final DecimalFormat myPosFormatter = new DecimalFormat("000.0000000");
    private final DecimalFormat mySpeedFormatter = new DecimalFormat("#0.0");
    private final DecimalFormat myDistFormatter = new DecimalFormat("#0.00000");

    private final String fileName;
    
    public FileTrackWriter(String file) {
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
            return pos.getTimestamp() +
            " " + myPosFormatter.format(Math.abs(pos.getLatitude())) + ((pos.getLatitude()>0)?"N":"S") +
            " " + myPosFormatter.format(Math.abs(pos.getLongitude())) + ((pos.getLongitude()>0)?"E":"W") +
            " " + (anchor?"A":"T") +
            " " + myDistFormatter.format(dist) +
            " " + mySpeedFormatter.format(speed) +
            " " + mySpeedFormatter.format(maxSpeed) +
            " " + interval +
            "\r\n";
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
