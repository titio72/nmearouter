/*
 * Copyright (c) 2021,  Andrea Boni
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

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.PilotMode;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.PGNDataParseException;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class EVOImpl implements EVO {

    private final TimestampProvider tp;
    private final int src;

    @Inject
    public EVOImpl(@NotNull TimestampProvider tp, int src) {
        this.tp = tp;
        this.src = src;
    }

    @Override
    public N2KMessage getAUTOMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.AUTO);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }

    @Override
    public N2KMessage getSTDBYMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.STANDBY);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }

    @Override
    public N2KMessage getLockHeadingMessage(double heading) {
        return new N2K126208RequestLockedHeading(src, Instant.ofEpochMilli(tp.getNow()), heading);
    }

    @Override
    public N2KMessage getWindDatumMessage(double windAngle) {
        return new N2K126208RequestWindDatum(src, Instant.ofEpochMilli(tp.getNow()), windAngle);
    }

    @Override
    public N2KMessage getVANEMessage() {
        try {
            return new N2K126208RequestPilotMode(src, Instant.ofEpochMilli(tp.getNow()), PilotMode.VANE);
        } catch (PGNDataParseException ignored) {
            return null;
        }
    }
}
