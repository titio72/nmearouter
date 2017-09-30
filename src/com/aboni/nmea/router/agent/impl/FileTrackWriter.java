package com.aboni.nmea.router.agent.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.agent.TrackWriter;
import com.aboni.utils.ServerLog;

public class FileTrackWriter implements TrackWriter {

    private DecimalFormat myFormatter = new DecimalFormat("#.0000000");
    private SimpleDateFormat tsFormatter;
    
    private String fileName;
    
    public FileTrackWriter(String file) {
        tsFormatter = new SimpleDateFormat("ddMMyy HHmmss.SSS");
        tsFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        fileName = file;
    }
    
    @Override
    public void write(GeoPositionT p, boolean anchor, double dist, double speed, double maxSpeed, int interval) {
        String msg = getPositionString(p, anchor);
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

    private String getPositionString(GeoPositionT pos, boolean anchor) {
        String msg = 
                System.currentTimeMillis() + 
                " " + tsFormatter.format(new Date(pos.getTimestamp())) + 
                " " + myFormatter.format(Math.abs(pos.getLatitude())) + 
                " " + ((pos.getLatitude()>0)?"N":"S") + 
                " " + myFormatter.format(Math.abs(pos.getLongitude())) +
                " " + ((pos.getLongitude()>0)?"E":"W") +
                /*" " + (staticPos?"A":"T") + */ /* set "A - Anchor" or "T - Travel" */ 
                "\r\n"; 
        return msg;
    }
    
    @Override
    public boolean init() {
        return true;
    }

    @Override
    public void dispose() {
    }

}
