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
import com.aboni.utils.TimestampProvider;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.message.Message;
import com.aboni.data.Pair;

import javax.inject.Inject;

public class NMEAGenericFilterProc implements NMEAPostProcess {

    private final NMEAFilter filter;
    private final TimestampProvider timestampProvider;
    private final RouterMessageFactory messageFactory;

    @Inject
    public NMEAGenericFilterProc(TimestampProvider timestampProvider, NMEAFilter filter, RouterMessageFactory messageFactory) {
        if (timestampProvider==null) throw new IllegalArgumentException("Timestamp provider is null");
        if (filter==null) throw new IllegalArgumentException("Filter is null");
        if (messageFactory==null) throw new IllegalArgumentException("Message factory is null");
        this.timestampProvider = timestampProvider;
        this.filter = filter;
        this.messageFactory = messageFactory;
    }

    private static final Pair<Boolean, Message[]> OK = new Pair<>(true, null);
    private static final Pair<Boolean, Message[]> KO = new Pair<>(false, null);

    @Override
    public Pair<Boolean, Message[]> process(Message message, String src) throws NMEARouterProcessorException {
        try {
            return (filter.match(messageFactory.createMessage(message, src, timestampProvider.getNow())) ? OK : KO);
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Error processing filter for sentence \"" + message + "\"", e);
        }
    }

    @Override
    public void onTimer() {
        // nothing to do on timer
    }
}
