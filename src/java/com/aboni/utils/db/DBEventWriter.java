package com.aboni.utils.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface DBEventWriter {

    void write(Event e, Connection c) throws SQLException;

    void reset();
}
