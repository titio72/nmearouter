package com.aboni.nmea.router;

public class SatInfo {

    public String getId() {
        return id;
    }

    public int getElevation() {
        return elevation;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public int getNoise() {
        return noise;
    }

    public boolean isUsed() {
        return used;
    }

    public GPSSat getSat() {
        return sat;
    }

    public SatInfo(String id, int elevation, int azimuth, int noise, boolean used) {
        this.id = id;
        this.elevation = elevation;
        this.azimuth = azimuth;
        this.noise = noise;
        this.used = used;
        this.sat = GPSSatsRepository.getSat(Integer.parseInt(id));
    }

    private final String id;
    private final int elevation;
    private final int azimuth;
    private final int noise;
    private final boolean used;
    private final GPSSat sat;
}
