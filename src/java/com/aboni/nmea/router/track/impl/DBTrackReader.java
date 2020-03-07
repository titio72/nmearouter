package com.aboni.nmea.router.track.impl;

import com.aboni.geo.GeoPositionT;
import com.aboni.nmea.router.track.TrackManagementException;
import com.aboni.nmea.router.track.TrackPoint;
import com.aboni.nmea.router.track.TrackReader;
import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBHelper;
import net.sf.marineapi.nmea.util.Position;

import javax.validation.constraints.NotNull;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTrackReader implements TrackReader {

    public interface StatementProvider {
        PreparedStatement getStatement(DBHelper db) throws SQLException;
    }

    private StatementProvider stProvider;

    public DBTrackReader(@NotNull StatementProvider stProvider) {
        this.stProvider = stProvider;
    }

    @Override
    public void readTrack(@NotNull TrackReaderListener target) throws TrackManagementException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement st = stProvider.getStatement(db)) {
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        target.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error("Error reading track", e);
            throw new TrackManagementException("Error reading track", e);
        }

    }

    private TrackPoint getSample(ResultSet rs) throws SQLException {
        return TrackPoint.newInstanceWithEngine(
                new GeoPositionT(rs.getTimestamp("TS").getTime(), new Position(rs.getDouble("lat"), rs.getDouble("lon"))),
                1 == rs.getInt("anchor"),
                rs.getDouble("dist"),
                rs.getDouble("speed"),
                rs.getDouble("maxSpeed"),
                rs.getInt("dTime"),
                EngineStatus.valueOf(rs.getInt("engine")));
    }
}
