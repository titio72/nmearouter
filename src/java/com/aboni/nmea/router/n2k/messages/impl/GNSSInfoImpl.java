package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.message.GNSSInfo;
import net.sf.marineapi.nmea.util.Position;

class GNSSInfoImpl implements GNSSInfo {

    private Position position;
    private double cog = Double.NaN;
    private double sog = Double.NaN;

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public double getCOG() {
        return cog;
    }

    @Override
    public double getSOG() {
        return sog;
    }

    void setCOG(double cog) {
        this.cog = cog;
    }

    void setSOG(double sog) {
        this.sog = sog;
    }

    void setPosition(Position p) {
        position = p;
    }
}
