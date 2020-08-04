/*
(C) 2020, Andrea Boni
This file is part of NMEARouter.
NMEARouter is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
NMEARouter is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
*/

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
