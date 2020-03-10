package com.aboni.nmea.router.track;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.impl.TrackPointBuilderImpl;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class TrackAnalyticsTest {

    //lat,lon,TS,id,anchor,dTime,dist,speed,tripid,maxSpeed,engine
    //43.6775303,10.2739860,"2020-02-08 06:49:58",299541,1,1,0.00003525,0.13,135,0.13,0
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void test2LegsTrip() throws Exception {
        TrackAnalytics a = new TrackAnalytics("test");
        try (BufferedReader in = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("trip135.csv")))) {
            //skip header
            in.readLine();
            String l = null;
            while ((l = in.readLine()) != null) {
                String[] p = l.split(",");
                TrackPoint tp = new TrackPointBuilderImpl()
                        .withPosition(new GeoPositionT(df.parse(p[2].substring(1, 20)).getTime(), Double.parseDouble(p[0]), Double.parseDouble(p[1])))
                        .withAnchor("1".equalsIgnoreCase(p[4]))
                        .withDistance(Double.parseDouble(p[6]))
                        .withSpeed(Double.parseDouble(p[7]), Double.parseDouble(p[9]))
                        .withPeriod(Integer.parseInt(p[5])).getPoint();
                a.processSample(tp);
            }
            assertEquals(2, a.getStats().legs.size());
            assertEquals(87.0174665, a.getStats().totalNavigationDistance, 0.000001);
        }
    }


}
