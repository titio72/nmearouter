package com.aboni.geo;

import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class TrackLoaderDB implements TrackLoader {

	private final PositionHistory h;
	
	public TrackLoaderDB() {
        h = new PositionHistory(0);
	}
	
	/* (non-Javadoc)
	 * @see com.aboni.geo.TrackLoader#load(java.util.Calendar, java.util.Calendar)
	 */
	@Override
	public boolean load(Calendar from, Calendar to) {
		boolean res;
		try (DBHelper db = new DBHelper(true)) {
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
					if (t == ts.getTime()) {
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
		} catch (SQLException | ClassNotFoundException e) {
			ServerLog.getLogger().Error("Cannot query Track DB!", e);
			res = false;
		}
	    return res;
	}

	@Override
	public PositionHistory getTrack() {
		return h;
	}
}
