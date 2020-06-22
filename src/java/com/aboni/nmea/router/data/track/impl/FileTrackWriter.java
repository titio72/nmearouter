/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.aboni.nmea.router.data.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.data.track.TrackPoint;
import com.aboni.nmea.router.data.track.TrackWriter;
import com.aboni.utils.ServerLog;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;

public class FileTrackWriter implements TrackWriter {

    private static final DecimalFormat myPosFormatter = new DecimalFormat("000.0000000");
    private static final DecimalFormat mySpeedFormatter = new DecimalFormat("#0.0");
    private static final DecimalFormat myDistFormatter = new DecimalFormat("#0.00000");

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
}
