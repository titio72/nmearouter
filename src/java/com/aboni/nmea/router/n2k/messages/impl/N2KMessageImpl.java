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

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.Instant;

public abstract class N2KMessageImpl implements N2KMessage {

    protected final N2KMessageHeader header;
    protected final byte[] data;

    private static class DefaultHeader implements N2KMessageHeader {
        final int pgn;
        final Instant now;

        DefaultHeader(int pgn, Instant now) {
            this.pgn = pgn;
            this.now = now;
        }

        @Override
        public int getPgn() {
            return pgn;
        }

        @Override
        public int getSource() {
            return 0;
        }

        @Override
        public int getDest() {
            return 255;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public Instant getTimestamp() {
            return now;
        }
    }

    protected static N2KMessageHeader getDefaultHeader(int pgn) {
        return new DefaultHeader(pgn, Instant.now());
    }

    protected N2KMessageImpl(N2KMessageHeader header, byte[] data) {
        this.data = data;
        this.header = header;
    }

    @Override
    public N2KMessageHeader getHeader() {
        return header;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        if (getHeader() != null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : getData()) {
                stringBuilder.append(String.format(" %x", (b & 0xFF)));
            }
            return String.format("PGN {%d} Source {%d} Data {%s}", getHeader().getPgn(), getHeader().getSource(), stringBuilder.toString());
        } else
            return super.toString();
    }

    @Override
    public String getMessageType() {
        return String.format("%d", header.getPgn());
    }

    @Override
    public String getMessageOrigin() {
        return String.format("%d", header.getSource());
    }
}
