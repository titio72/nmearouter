package com.aboni.nmea.router.n2k;

public class PGNDataParseException extends Exception {
    public PGNDataParseException(long pgn) {
        super(String.format("PGN %d is unsupported", pgn));
    }

    public PGNDataParseException(String msg) {
        super(msg);
    }

    public PGNDataParseException(String msg, Throwable t) {
        super(msg, t);
    }
}
