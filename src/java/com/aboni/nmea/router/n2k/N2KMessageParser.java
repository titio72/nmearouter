package com.aboni.nmea.router.n2k;

public interface N2KMessageParser {

    void addMessage(N2KMessage msg) throws PGNDataParseException;

    void addString(String s) throws PGNDataParseException;

    N2KMessageHeader getHeader();

    int getLength();

    byte[] getData();

    boolean needMore();

    boolean isSupported();

    N2KMessage getMessage() throws PGNDataParseException;
}
