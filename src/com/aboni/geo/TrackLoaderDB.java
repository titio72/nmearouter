package com.aboni.geo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;

import com.aboni.utils.DBHelper;
import com.aboni.utils.ServerLog;

public class TrackLoaderDB implements TrackLoader {

	private PositionHistory h;
	
	public TrackLoaderDB() {
        h = new PositionHistory(0);
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.geo.TrackLoader#load(java.util.Calendar, java.util.Calendar)
	 */
	@Override
	public boolean load(Calendar from, Calendar to) {
	    DBHelper db = null;
	    boolean res = false;
	    try {
	        db = new DBHelper(true);
    	    PreparedStatement st = db.getConnection().prepareStatement(
    	            "select lat, lon, TS, dTime anchor from track where TS >= ? and TS <= ?");
    	    
    	    st.setTimestamp(1, new Timestamp(from.getTimeInMillis()));
    	    st.setTimestamp(2, new Timestamp(to.getTimeInMillis()));
    	    
    	    long t = 0;
    	    if (st.execute()) {
    	        ResultSet rs = st.getResultSet();
    	        while (rs.next()) {
                    double lat = rs.getDouble(1);
                    double lon = rs.getDouble(2);
                    Timestamp ts = rs.getTimestamp(3);
                    long dTime = rs.getLong(4);
                    if (t==ts.getTime()) {
                    	t += dTime * 1000;
                    } else {
                    	t = ts.getTime();
                    }
                    //boolean anchor = (rs.getInt(6)==1);
    	            GeoPositionT p = new GeoPositionT(t, lat, lon);
                    h.addPosition(p);
    	        }
    	    }
    	    res = true;
	    } catch (Exception e) {
	        ServerLog.getLogger().Error("Cannot query Track DB!", e);
	        res = false;
	    } finally {
	        try {
                db.close();
            } catch (Exception e) { }
	    }
	    return res;
	}

	/* (non-Javadoc)
	 * @see com.aboni.geo.TrackLoader#getTrack()
	 */
	@Override
	public PositionHistory getTrack() {
		return h;
	}
}
