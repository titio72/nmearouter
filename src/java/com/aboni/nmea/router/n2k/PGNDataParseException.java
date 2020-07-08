package com.aboni.nmea.router.n2k;

public class PGNDataParseException extends Exception {

    private final boolean unsupported;

    public PGNDataParseException(long pgn) {
        super(String.format("PGN %d is unsupported", pgn));
        unsupported = true;
    }

    public boolean isUnsupported() {
        return unsupported;
    }

    public PGNDataParseException(String msg) {
        super(msg);
        unsupported = false;
    }

    public PGNDataParseException(String msg, Throwable t) {
        super(msg, t);
        unsupported = false;
    }
}
