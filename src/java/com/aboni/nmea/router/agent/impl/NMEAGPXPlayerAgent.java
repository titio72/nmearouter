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

package com.aboni.nmea.router.agent.impl;

import com.aboni.geo.Course;
import com.aboni.geo.GeoPositionT;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import net.sf.marineapi.nmea.util.CompassPoint;
import net.sf.marineapi.nmea.util.FaaMode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NMEAGPXPlayerAgent extends NMEAAgentImpl {

    private final SimpleDateFormat fmt;
    private final TimestampProvider timestampProvider;
    private final Log log;
    private GeoPositionT prevPos;
    private long t0Play;
    private long t0;
    private String file;
    private boolean stop;

    @Inject
    public NMEAGPXPlayerAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, true, false);
        this.log = log;
        this.timestampProvider = tp;
        this.fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        this.fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public void setFile(@NotNull String file) {
        if (this.file == null) {
            getLogBuilder().wO("init").wV("file", file).info(log);
            this.file = file;
        } else {
            getLogBuilder().wO("init").wV("error", file).wV("error", "already initialized").error(log);
        }
    }

    @Override
    public String getDescription() {
        return (file != null) ? ("File " + file) : "";
    }

    @Override
    public String getType() {
        return "GPS Player";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    protected boolean onActivate() {
        synchronized (this) {
            stop = false;
        }
        return play();
    }

    @Override
    protected void onDeactivate() {
        synchronized (this) {
            stop = true;
        }
    }

    public boolean play() {
        final Document d;
        try {
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            df.setFeature("http://xml.org/sax/features/external-general-entities", false);
            DocumentBuilder builder = df.newDocumentBuilder();
            d = builder.parse(new InputSource(file));
            d.getDocumentElement().normalize();
        } catch (Exception e) {
            getLogBuilder().wO("play gpx").error(log, e);
            return false;
        }

        Thread t = new Thread(() -> {
            Element gpx = d.getDocumentElement();
            NodeList tracks = gpx.getElementsByTagName("trk");
            Element track = (Element)tracks.item(0);
            NodeList segments = track.getElementsByTagName("trkseg");
            for (int i = 0; i<segments.getLength(); i++) {
                if (isStop()) break;
                Element segment = (Element)segments.item(i);
                NodeList points = segment.getElementsByTagName("trkpt");
                for (int j = 1; j<points.getLength(); j++) {
                    try {
                        Element p = (Element)points.item(j);
                        Node t1 = p.getElementsByTagName("time").item(0);
                        String sTime = t1.getTextContent();
                        Date dPos = fmt.parse(sTime);
                        GeoPositionT pos = new GeoPositionT(dPos.getTime(), Double.parseDouble(p.getAttribute("lat")), Double.parseDouble(p.getAttribute("lon")));
                        doIt(pos);
                    } catch (Exception e) {
                        getLogBuilder().wO("play gpx").error(log, e);
                    }
                }
            }
            if (!isStop()) {
                stop();
            }
        });
        t.setDaemon(true);
        t.start();

        return true;
    }


    protected boolean isStop() {
        synchronized (this) {
            return stop;
        }
    }

    private void doIt(GeoPositionT pos) {
        if (t0==0) {
            t0 = timestampProvider.getNow();
            t0Play = pos.getTimestamp();
        }
        if (prevPos!=null) {
            long dt = pos.getTimestamp() - t0Play;
            long elapsed = timestampProvider.getNow() - t0;
            Utils.pause((int) (dt - elapsed));
            Course c = new Course(prevPos, pos);
            RMCSentence s = (RMCSentence) SentenceFactory.getInstance().createParser(TalkerId.GP, SentenceId.RMC);
            s.setCourse(c.getCOG());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            cal.setTimeInMillis(pos.getTimestamp());
            s.setDate(new net.sf.marineapi.nmea.util.Date(
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)));
            s.setTime(new net.sf.marineapi.nmea.util.Time(
                    cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND)));
            s.setMode(FaaMode.AUTOMATIC);
            s.setSpeed(c.getSpeed());
            s.setPosition(pos);
            s.setVariation(0.0);
            s.setDirectionOfVariation(CompassPoint.EAST);
            notify(s);
        }
        prevPos = pos;
    }

}
