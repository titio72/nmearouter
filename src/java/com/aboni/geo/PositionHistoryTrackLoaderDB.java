package com.aboni.geo;

import com.aboni.nmea.router.agent.impl.track.EngineStatus;
import com.aboni.nmea.router.agent.impl.track.TrackPoint;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

public class PositionHistoryTrackLoaderDB implements PositionHistoryTrackLoader {

    private final PositionHistory<TrackPoint> h;

    public PositionHistoryTrackLoaderDB() {
        h = new PositionHistory<>(0);
	}
	
	/* (non-Javadoc)
     * @see com.aboni.geo.PositionHistoryTrackLoader#load(java.util.Calendar, java.util.Calendar)
	 */
	@Override
	public boolean load(Calendar from, Calendar to) {
		boolean res;
		try (DBHelper db = new DBHelper(true)) {
			try (PreparedStatement st = db.getConnection().prepareStatement(
					"select lat, lon, TS, anchor, speed, maxSpeed, dist, dTime, engine from track where TS >= ? and TS <= ?")) {
				st.setTimestamp(1, new Timestamp(from.getTimeInMillis()));
				st.setTimestamp(2, new Timestamp(to.getTimeInMillis()));
				if (st.execute()) {
					try (ResultSet rs = st.getResultSet()) {
						while (rs.next()) {
							double lat = rs.getDouble(1);
							double lon = rs.getDouble(2);
							Timestamp ts = rs.getTimestamp(3);
							int anchor = rs.getInt(4);
							double speed = rs.getDouble(5);
							double maxSpeed = rs.getDouble(6);
							double dist = rs.getDouble(7);
							int dTme = rs.getInt(8);
							EngineStatus eng = EngineStatus.valueOf((byte) rs.getInt(9));
							GeoPositionT p = new GeoPositionT(ts.getTime(), lat, lon);
							TrackPoint point = TrackPoint.newInstanceWithEngine(p, anchor == 1, dist, speed, maxSpeed, dTme, eng);
							doWithPoint(point);
							h.addPosition(p);
						}
					}
				}
				res = true;
			}
		} catch (SQLException | ClassNotFoundException e) {
			ServerLog.getLogger().error("Cannot query Track DB!", e);
			res = false;
		}
	    return res;
	}

    private void doWithPoint(TrackPoint p) {
        h.addPosition(p.getPosition(), p);
    }


	@Override
	public PositionHistory getTrack() {
		return h;
	}
}
