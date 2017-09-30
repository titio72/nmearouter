package com.aboni.nmea.router.agent;

import com.mysql.jdbc.PreparedStatement;

public interface EventWriter {
    void write(Event e, PreparedStatement stm);
}
