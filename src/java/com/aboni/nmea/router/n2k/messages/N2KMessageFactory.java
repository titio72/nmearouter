package com.aboni.nmea.router.n2k.messages;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

public interface N2KMessageFactory {

    boolean isSupported(int pgn);

    boolean isFast(int pgn);

    N2KMessage newUntypedInstance(N2KMessageHeader h, byte[] data);

    N2KMessage newInstance(N2KMessageHeader h, byte[] data) throws PGNDataParseException;

    N2KMessage newInstance(int pgn, byte[] data) throws PGNDataParseException;

}
