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

package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.*;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.google.common.hash.HashCode;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class N2KStreamImpl implements N2KStream {

    private static final long MAX_AGE = 750L;
    private static final long MIN_AGE = 250L;

    private boolean throttling;

    private static class Payload {
        int hashcode;
        long timestamp;
    }

    private final Map<Integer, Payload> payloadMap;
    private Log logger;
    private final PGNSourceFilter srcFilter;
    private final N2KMessageParserFactory parserFactory;

    @Inject
    public N2KStreamImpl(@NotNull Log log, @NotNull N2KMessageParserFactory parserFactory) {
        this(log, false, parserFactory);
    }

    public N2KStreamImpl(Log logger, boolean throttling, @NotNull N2KMessageParserFactory parserFactory) {
        this.parserFactory = parserFactory;
        this.logger = logger;
        payloadMap = new HashMap<>();
        srcFilter = new PGNSourceFilter(logger);
        this.throttling = throttling;
    }

    public void setThrottling(boolean throttling) {
        this.throttling = throttling;
    }

    public void setLogger(@NotNull Log logger) {
        this.logger = logger;
    }

    @Override
    public N2KMessage getMessage(String sMessage) {
        try {
            N2KMessageParser p = parserFactory.getNewParser();
            p.addString(sMessage);
            int pgn = p.getHeader().getPgn();
            if (p.isSupported() && srcFilter.accept(p.getHeader().getSource(), pgn) &&
                    isSend(pgn, p.getHeader().getTimestamp().toEpochMilli(), p.getData())) {
                return p.getMessage();
            } else return null;
        } catch (Exception e) {
            logger.error(LogStringBuilder.start("N2KStream").wO("parse").wV("message", sMessage).toString(), e);
            return null;
        }
    }

    private static int hashCodeOf(byte[] data) {
        return HashCode.fromBytes(data).hashCode();
    }

    private boolean isSend(int pgn, long ts, byte[] data) {
        if (throttling) {
            Payload p = payloadMap.getOrDefault(pgn, null);
            if (p == null) {
                p = new Payload();
                p.timestamp = ts;
                p.hashcode = hashCodeOf(data);
                payloadMap.put(pgn, p);
                return true;
            } else {
                int hash = hashCodeOf(data);
                // check for minimum age (active throttling) then check for maximum age or some changes
                if ((ts - MIN_AGE) > p.timestamp &&
                        ((ts - MAX_AGE) > p.timestamp || hash != p.hashcode)) {
                    p.timestamp = ts;
                    p.hashcode = hash;
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
