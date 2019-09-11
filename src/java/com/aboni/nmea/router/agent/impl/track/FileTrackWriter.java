package com.aboni.nmea.router.agent.impl.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.utils.ServerLog;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

public class FileTrackWriter implements TrackWriter {

    private final DecimalFormat myPosFormatter = new DecimalFormat("000.0000000");
    private final DecimalFormat mySpeedFormatter = new DecimalFormat("#0.0");
    private final DecimalFormat myDistFormatter = new DecimalFormat("#0.00000");

    private final String fileName;
    
    public FileTrackWriter(String file) {
        fileName = file;
    }
    
    @Override
    public void write(TrackPoint point) {
        String msg = getPositionString(point);
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
            ServerLog.getLogger().error("Cannot write position log", e);
        }
    }

    private String getPositionString(TrackPoint point) {
        if (point != null) {
            GeoPositionT pos = point.getPosition();
            return pos.getTimestamp() +
            " " + myPosFormatter.format(Math.abs(pos.getLatitude())) + ((pos.getLatitude()>0)?"N":"S") +
            " " + myPosFormatter.format(Math.abs(pos.getLongitude())) + ((pos.getLongitude()>0)?"E":"W") +
                    " " + (point.isAnchor() ? "A" : "T") +
                    " " + myDistFormatter.format(point.getDistance()) +
                    " " + mySpeedFormatter.format(point.getMaxSpeed()) +
                    " " + mySpeedFormatter.format(point.getMaxSpeed()) +
                    " " + point.getPeriod() +
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
        // nothing to dispose
    }
}
