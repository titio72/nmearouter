/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.n2k.evo;

import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.impl.N2KMessageImpl;

import java.time.Instant;

public class N2K126208RequestPilotMode extends N2KMessageImpl {

    private final PilotMode mode;

    public N2K126208RequestPilotMode(int src, Instant time, PilotMode mode) throws PGNDataParseException {
        super(new N2kHeader126208(src, time), getDataForMode(mode));
        this.mode = mode;
    }

    private static byte[] getDataForMode(PilotMode mode) throws PGNDataParseException {
        if (mode==PilotMode.STANDBY) {
            return new byte[]{
                    (byte) 0x01,  // 126208 type (1 means "command")
                    (byte) 0x63,  // PGN 65379
                    (byte) 0xff,  // PGN 65379
                    (byte) 0x00,  // PGN 65379
                    (byte) 0xf8,  // priority + reserved
                    (byte) 0x04,  // 4 params
                    (byte) 0x01,  // first param - 1 of PGN 65379 (manufacturer code)
                    (byte) 0x3b,  // 1851 Raymarine
                    (byte) 0x07,  // 1851 Raymarine
                    (byte) 0x03,  // second param -  3 of pgn 65369 (Industry code)
                    (byte) 0x04,  // Ind. code 4
                    (byte) 0x04,  // third parameter - 4 of pgn 65379 (mode)
                    (byte) 0x00,  // code 0 (STDBY)
                    (byte) 0x00,  // fourth param - 0 (does not exists, seems to be a Raymarine hack)
                    (byte) 0x05   // value of weird raymarine param
            };
        } else if (mode==PilotMode.VANE) {
            return new byte[]{
                    (byte) 0x01,  // 126208 type (1 means "command")
                    (byte) 0x63,  // PGN
                    (byte) 0xff,  // PGN
                    (byte) 0x00,  // PGN
                    (byte) 0xf8,  // priority + reserved
                    (byte) 0x04,  // n params
                    (byte) 0x01,  // param 1 (manufacturer code)
                    (byte) 0x3b,  // 1851 Raymarine
                    (byte) 0x07,  // 1851 Raymarine
                    (byte) 0x03,  // parameter 3 (Industry code)
                    (byte) 0x04,  // Ind. code 4
                    (byte) 0x04,  // parameter 4 (mode)
                    (byte) 0x00,  // code 0 (VANE)
                    (byte) 0x01,  // parameter 1 (???? reverse engineered)
                    (byte) 0x05   // finish
            };
        } else if (mode==PilotMode.AUTO) {
            return new byte[] {
                    (byte) 0x01,  // 126208 type (1 means "command")
                    (byte) 0x63,  // PGN
                    (byte) 0xff,  // PGN
                    (byte) 0x00,  // PGN
                    (byte) 0xf8,  // priority + reserved
                    (byte) 0x04,  // n params
                    (byte) 0x01,  // param 1 (manufacturer code)
                    (byte) 0x3b,  // 1851 Raymarine
                    (byte) 0x07,  // 1851 Raymarine
                    (byte) 0x03,  // parameter 3 (Industry code)
                    (byte) 0x04,  // Ind. code 4
                    (byte) 0x04,  // parameter 4 (mode)
                    (byte) 0x40,  // code 40 (AUTO)
                    (byte) 0x00,  // fourth param - 0 (does not exists, seems to be a Raymarine hack)
                    (byte) 0x05   // value of weird raymarine param
            };
        } else {
            throw new PGNDataParseException("Usupported pilot mode");
        }
    }

    public PilotMode getMode() {
        return mode;
    }

    @Override
    public String getMessageContentType() {
        return "RequestPilotMode";
    }
}
