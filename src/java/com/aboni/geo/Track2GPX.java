package com.aboni.geo;

import com.aboni.geo.PositionHistory.DoWithPoint;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Track2GPX implements TrackDumper {

	public class PointWriter implements DoWithPoint {

		final Writer theWriter;
		
		PointWriter(Writer w) {
			theWriter = w;
		}
		
		@Override
		public void doWithPoint(GeoPositionT p) {
			try {
				handlePoint(p, theWriter);
			} catch (IOException e) {
				// ??????
			}
		}

	}

	private PositionHistory track;
	private String trackName = DEFAULT_TRACK_NAME;
	public static final String DEFAULT_TRACK_NAME = "track";
	
	public Track2GPX() {
		// nothing to init
	}

	/* (non-Javadoc)
	 * @see com.aboni.geo.TrackDumper#setTrack(com.aboni.geo.PositionHistory)
	 */
	@Override
	public void setTrack(PositionHistory track) {
		this.track = track;
	}

	/* (non-Javadoc)
	 * @see com.aboni.geo.TrackDumper#dump(java.io.Writer)
	 */
	@Override
	public void dump(Writer w) throws IOException {
		if (track!=null) {
			writeHeader(w);
			writePoints(w);
			writeFooter(w);
		}
	}

	private void writeFooter(Writer w) throws IOException {
        String footer = "</trkseg></trk></gpx>";
        w.write(footer);
	}

	private void writePoints(Writer w) {
		track.iterate(new PointWriter(w));
		
	}
	
	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private GeoPositionT previous;
    private GeoPositionT lastTrack;
	private static final boolean TRACK_THEM_ALL = true;
	
	private boolean trackIt(GeoPositionT p, GeoPositionT pr) {
        boolean trackIt = true;
        if (pr!=null) {
            Course c = new Course(pr, p);
            trackIt = (c.getDistance()>0.0025 /* NMg ~5m*/); 
        }
        return trackIt;
	}
	
	@SuppressWarnings("PointlessArithmeticExpression")
	private void handlePoint(GeoPositionT p, Writer w) throws IOException {
		if (TRACK_THEM_ALL ) {
    	    writePoint(p, w);
		} else {
		    if (trackIt(p, previous)) {
		        if (lastTrack!=null && (p.getTimestamp()-lastTrack.getTimestamp())>1000*60*60*1) {
		            w.write("</trkseg><trkseg>");
		            writePoint(previous, w);
		        }
		        lastTrack = p;
	    	    writePoint(p, w);
		    }
		}
        previous = p;
	}

    private void writePoint(GeoPositionT p, Writer w) throws IOException {
        String s = "<trkpt lat=\"" + p.getLatitude() + 
            		"\" lon=\"" + p.getLongitude() + 
            		"\"><time>" + df.format(new Date(p.getTimestamp())) + 
            		"</time></trkpt>\n";
        w.write(s);
    }

	private void writeHeader(Writer w) throws IOException {
		String header = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><gpx xmlns=\"http://www.topografix.com/GPX/1/1\" creator=\"MapSource 6.15.5\" version=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\"><trk>\n";
		String name = "<name>" + trackName + "</name><trkseg>\n";		
        w.write(header);
        w.write(name);
	}

	@Override
	public void setTrackName(String trackName) {
		this.trackName = trackName;
	}
	
	
}

/*
<?xml version="1.0" encoding="UTF-8" standalone="no" ?>

<gpx xmlns="http://www.topografix.com/GPX/1/1" xmlns:gpxx="http://www.garmin.com/xmlschemas/GpxExtensions/v3" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1" creator="Oregon 400t" version="1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd http://www.garmin.com/xmlschemas/GpxExtensions/v3 http://www.garmin.com/xmlschemas/GpxExtensionsv3.xsd http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www.garmin.com/xmlschemas/TrackPointExtensionv1.xsd">
  <metadata>
    <link href="http://www.garmin.com">
      <text>Garmin International</text>
    </link>
    <time>2009-10-17T22:58:43Z</time>
  </metadata>
  <trk>
    <name>Example GPX Document</name>
    <trkseg>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>4.46</ele>
        <time>2009-10-17T18:37:26Z</time>
      </trkpt>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>4.94</ele>
        <time>2009-10-17T18:37:31Z</time>
      </trkpt>
      <trkpt lat="47.644548" lon="-122.326897">
        <ele>6.87</ele>
        <time>2009-10-17T18:37:34Z</time>
      </trkpt>
    </trkseg>
  </trk>
</gpx>*/


/*

      <LineString>
        <extrude>1</extrude>
        <tessellate>1</tessellate>
        <altitudeMode>absolute</altitudeMode>
        <coordinates> -112.2550785337791,36.07954952145647,2357
          -112.2549277039738,36.08117083492122,2357
          -112.2552505069063,36.08260761307279,2357
          -112.2564540158376,36.08395660588506,2357
          -112.2580238976449,36.08511401044813,2357
          -112.2595218489022,36.08584355239394,2357
          -112.2608216347552,36.08612634548589,2357
          -112.262073428656,36.08626019085147,2357
          -112.2633204928495,36.08621519860091,2357
          -112.2644963846444,36.08627897945274,2357
          -112.2656969554589,36.08649599090644,2357 
        </coordinates>
      </LineString>


*/