package com.aboni.nmea.router.n2k.messages;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import javax.validation.constraints.NotNull;

public interface N2KMessageFactory {

    boolean isSupported(int pgn);

    boolean isFast(int pgn);

    N2KMessage newUntypedInstance(@NotNull N2KMessageHeader h, @NotNull byte[] data);

    N2KMessage newInstance(@NotNull N2KMessageHeader h, @NotNull byte[] data) throws PGNDataParseException;

    N2KMessage newInstance(int pgn, @NotNull byte[] data) throws PGNDataParseException;

}
