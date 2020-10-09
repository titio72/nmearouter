package com.aboni.toolkit;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.NMEARouterModule;
import com.aboni.nmea.router.data.track.*;
import com.aboni.nmea.router.data.track.impl.DBTrackEventWriter;
import com.aboni.nmea.router.data.track.impl.DBTripEventWriter;
import com.aboni.nmea.router.data.track.impl.TrackManagerImpl;
import com.aboni.nmea.router.data.track.impl.TripManagerXImpl;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.ThingsFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import net.sf.marineapi.nmea.util.Position;

import java.io.FileReader;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadGPX {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new NMEARouterModule());
        ThingsFactory.setInjector(injector);


        int counter = 0;
        String file = "/home/aboni/Downloads/nn.gpx";
        try (FileReader reader = new FileReader(file)) {
            byte[] buffer = new byte[6];
            StringBuilder b = null;
            int c;
            while ((c = reader.read()) != -1) {
                if (counter == 6) {
                    buffer[0] = buffer[1];
                    buffer[1] = buffer[2];
                    buffer[2] = buffer[3];
                    buffer[3] = buffer[4];
                    buffer[4] = buffer[5];
                    buffer[5] = (byte) c;
                    String s = new String(buffer);
                    if ("<trkpt".equals(s)) {
                        b = new StringBuilder("<trkpt");
                    } else if ("trkpt>".equals(s) && b!=null) {
                        b.append(">");
                        processPoint(b.toString());
                        b = null;
                    } else if (b != null) {
                        b.append(new String(new byte[]{(byte) c}));
                    }
                } else {
                    buffer[counter] = (byte) c;
                    counter++;
                }
            }
        } catch (Exception e) {
            ConsoleLog.getLogger().error("Error", e);
        }
    }

    private static final String PATTERN = "lat=\"(.+)\" lon=\"(.+)\".+time>(.+)</time>.*navionics_speed>(.+)</navionics_speed";
    private static final Pattern RX = Pattern.compile(PATTERN);

    private static void processPoint(String toString) {
        Matcher m = RX.matcher(toString);

        if (m.find()) {
            double lat = Double.parseDouble(m.group(1));
            double lon = Double.parseDouble(m.group(2));
            Instant time = Instant.parse(m.group(3));
            double speed = Double.parseDouble(m.group(4)) * 3600.0 / 1852.0;
            GeoPositionT g = new GeoPositionT(time.toEpochMilli(), new Position(lat, lon));

            process(g, speed);


            ConsoleLog.getLogger().console(String.format("%s %s %s %s%n", m.group(1), m.group(2), m.group(3), m.group(4)));
        }
    }

    private static final TrackManager tracker = new TrackManagerImpl();

    private static final TripManagerX tripManager = new TripManagerXImpl("trip", "track",
            new DBTrackEventWriter("track"), new DBTripEventWriter("trip"));

    private static void process(GeoPositionT posT, double sog) {

        TrackPoint point = tracker.processPosition(posT, sog);
        if (point != null) {
            TrackPointBuilder builder = ThingsFactory.getInstance(TrackPointBuilder.class);
            point = builder.withPoint(point).withEngine(EngineStatus.UNKNOWN).getPoint();
            try {
                tripManager.onTrackPoint(new TrackEvent(point));
            } catch (TripManagerException e) {
                ConsoleLog.getLogger().error("Error handling point", e);
            }
        }
    }

}
