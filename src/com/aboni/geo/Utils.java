package com.aboni.geo;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.marineapi.nmea.util.Position;

public class Utils {

	private Utils() {
	}

	public static double normalizeDegrees0_360(double m) {
        if (m>360.0) {
            return m - (360*((int)(m/360)));
        } else if (m<0.0) {
            return m + (360*((int)(-m/360))) + 360;
        } else {
        	return m;
        }
    }

	public static double normalizeDegrees180_180(double m) {
        m = normalizeDegrees0_360(m);
        if (m>180.0) {
        	m -= 360.0;
        }
        return m;
    }
	
	/**
	 * 
	 * @param d
	 * @return
	 */
	public static String getLatitudeEmisphere(double d) {
		return d>0?"N":"S";
	}
	
	/**
	 * 
	 * @param d
	 * @return
	 */
	public static String getLongitudeEmisphere(double d) {
		return d>0?"E":"W";
	}
	
	/**
	 * Converts a latitude value with N or S indication to signed (+ for North a - for South)
	 * @param lat The unsigned latitude
	 * @param NS North or South. Accepted values are 'N', 'n', 'S' & 's'
	 * @return The signed latitude.
	 */
	public static double getSignedLatitude(double lat, char NS) {
		if (NS=='N' || NS=='N')
			return lat;
		else if (NS=='S' || NS=='s') 
			return -lat;
		else
			return 0.0;
	}

	
	/**
	 * Converts a longitude value with W or E indication to signed (+ for East a - for West)
	 * @param lon The unsigned longitude
	 * @param WE East or West. Accepted values are 'N', 'n', 'S' & 's'
	 * @return The signed longitude.
	 */
	public static double getSignedLongitude(double lon, char WE) {
		if (WE=='E' || WE=='e') 
			return lon;
		else  if (WE=='W' || WE=='w') 
			return -lon;
		else
			return 0.0;
	}
	
	/*
	public static GeoPosition getPosition(Position p) {
	    double lat = p.getLatitude();
		double lon = p.getLongitude();
		return new GeoPosition(lat, lon);
	}
	*/
	
	public static double getNormal(double ref, double angle) {
		boolean ref180 = ref < 0.0 && ref >= -180.0; 
		double res = com.aboni.misc.Utils.getNormal(ref, angle);
		if (ref180 && res>180.0) res -=360;
		return res;
	}
	
	
	public static Position calcNewLL(Position p0, double heading, double dist) {
		GeodesicData d = Geodesic.WGS84.Direct(p0.getLatitude(), p0.getLongitude(), heading, dist * 1852);
		return new Position(d.lat2, d.lon2);
	}
}
