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

package com.aboni.nmea.router.processors;

import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.filters.impl.PositionFilter;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.router.utils.SafeLog;
import com.aboni.utils.Pair;

import javax.inject.Inject;

public class PositionFilterProcessor implements NMEAPostProcess {

    private final PositionFilter filter;
    private final TimestampProvider timestampProvider;
    private final Log log;
    private final RouterMessageFactory messageFactory;

    @Inject
    public PositionFilterProcessor(Log log, TimestampProvider timestampProvider, RouterMessageFactory messageFactory) {
        if (timestampProvider==null) throw new IllegalArgumentException("Timestamp provider is null");
        if (messageFactory==null) throw new IllegalArgumentException("Message factory is null");
        this.log = SafeLog.getSafeLog(log);
        this.timestampProvider = timestampProvider;
        this.filter = new PositionFilter();
        this.messageFactory = messageFactory;
    }

    private static final Pair<Boolean, Message[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Message[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Message[]> process(Message sentence, String src) throws NMEARouterProcessorException {
        try {
            return (filter.match(messageFactory.createMessage(sentence, src, timestampProvider.getNow())) ? OK : KO);
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Cannot process RMC sentence for filtering \"" + sentence + "\"", e);
        }
    }

    private int i = 0;

    @Override
    public void onTimer() {
        i = (i+1) % 60;
        if (i==0) {
            filter.dumpStats(log);
        }
    }
}
