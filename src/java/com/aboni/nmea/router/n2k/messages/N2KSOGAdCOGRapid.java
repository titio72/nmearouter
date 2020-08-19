package com.aboni.nmea.router.n2k.messages;

public interface N2KSOGAdCOGRapid {

    int PGN = 129026;

    int getSID();

    double getSOG();

    double getCOG();

    String getCOGReference();

    boolean isTrueCOG();
}
