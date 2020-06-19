package com.aboni.nmea.router.n2k;

public class PGNDefParseException extends Exception {

    PGNDefParseException(String msg) {
        super(msg);
    }

    PGNDefParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
