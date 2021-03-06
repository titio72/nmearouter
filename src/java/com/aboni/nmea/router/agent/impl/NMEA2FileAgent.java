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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.sentences.NMEASentenceItem;
import com.aboni.utils.Log;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class NMEA2FileAgent extends NMEAAgentImpl {

    private static final long DUMP_PERIOD = 10L * 1000L;
    private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
    private final Log log;
    private long lastDump = 0;
    private final List<NMEASentenceItem> queue = new LinkedList<>();
    private final TimestampProvider timestampProvider;

    @Inject
    public NMEA2FileAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, true);
        this.log = log;
        this.timestampProvider = tp;
    }

    @Override
    public String getDescription() {
        return "Dump the NMEA stream to file";
    }

    @Override
    public String getType() {
        return "StreamDump";
    }

    @Override
    public String toString() {
        return "NMEA2FileAgent";
    }

    @OnRouterMessage
    public void onSentence(RouterMessage rm) {
        if (rm.getMessage() instanceof NMEA0183Message) {
            NMEASentenceItem e = new NMEASentenceItem(((NMEA0183Message) rm.getMessage()).getSentence(), timestampProvider.getNow(), "  ");
            synchronized (queue) {
                if (isStarted()) {
                    queue.add(e);
                    try {
                        dump();
                    } catch (IOException e1) {
                        getLogBuilder().wO("dump sentence").wV("sentence", rm.getMessage()).error(log, e1);
                    }
                }
            }
        }
    }

    private void dump() throws IOException {
        long t = timestampProvider.getNow();
        if (t - lastDump > DUMP_PERIOD) {
            lastDump = t;
            File f = new File("nmea" + df.format(new Date()) + ".log");
            long bytes = 0;
            try (FileWriter w = new FileWriter(f, true)) {
                for (NMEASentenceItem e : queue) {
                    String s = e.toString();
                    w.write(s);
                    w.write("\n");
                    bytes += s.length() + 1;
                }
                queue.clear();
                w.flush();
            }
            getLogBuilder().wO("dump").wV("bytes", bytes).wV("timestamp", new Date()).info(log);
        }
    }
}
