package com.aboni.nmea.router.track;

import com.aboni.sensors.EngineStatus;
import com.aboni.utils.ServerLog;
import com.aboni.utils.db.DBException;
import com.aboni.utils.db.DBHelper;

import javax.validation.constraints.NotNull;
import java.sql.*;

public class TrackReader {

    public interface TrackReaderListener {
        void onRead(TrackSample sample);
    }

    public interface StatementProvider {
        PreparedStatement getStatement(DBHelper db) throws SQLException;
    }

    public void readTrack(@NotNull TrackReaderListener tearget, @NotNull StatementProvider stProvider) throws DBException {
        try (DBHelper db = new DBHelper(true)) {
            try (PreparedStatement  st = stProvider.getStatement(db)) {
                try (ResultSet rs = st.executeQuery()) {
                    while (rs.next()) {
                        tearget.onRead(getSample(rs));
                    }
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            ServerLog.getLogger().error("Error reading track analytics", e);
            throw new DBException("Error reading track analytics", e);
        }

    }

    private TrackSample getSample(ResultSet rs) throws SQLException {
        TrackSample s = new TrackSample();
        s.ts = rs.getTimestamp(1).getTime();
        s.distance = rs.getDouble(2);
        s.speed = rs.getDouble(3);
        s.maxSpeed = rs.getDouble(4);
        s.eng = EngineStatus.valueOf(rs.getInt(5));
        s.anchor = 1 == rs.getInt(6);
        s.period = rs.getInt(7);
        return s;
    }
}
