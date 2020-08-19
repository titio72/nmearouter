package com.aboni.nmea.router.n2k;

public class Satellite {
    private final int id;
    private final int elevation;
    private final int azimuth;
    private final int srn;
    private final int status;

    public Satellite(int id, int elevation, int azimuth, int srn, int status) {
        this.id = id;
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.srn = srn;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getElevation() {
        return elevation;
    }

    public int getAzimuth() {
        return azimuth;
    }

    public int getSrn() {
        return srn;
    }

    public String getStatus() {
        switch (status) {
            case 0:
                return "Not tracked";
            case 1:
                return "Tracked";
            case 2:
                return "Used";
            case 3:
                return "Not tracked+Diff";
            case 4:
                return "Tracked+Diff";
            case 5:
                return "Used+Diff";
            default:
                return "Undef";
        }
    }
}
