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
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.sentences.NMEASentenceItem;

import javax.inject.Inject;
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
    private long lastDump = 0;
    private final List<NMEASentenceItem> queue = new LinkedList<>();

    @Inject
    public NMEA2FileAgent(Log log, TimestampProvider tp) {
        super(log, tp, false, true);
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
            NMEASentenceItem e = new NMEASentenceItem(((NMEA0183Message) rm.getMessage()).getSentence(), getTimestampProvider().getNow(), "  ");
            synchronized (queue) {
                if (isStarted()) {
                    queue.add(e);
                    try {
                        dump();
                    } catch (IOException e1) {
                        getLog().error(() -> getLogBuilder().wO("dump sentence").wV("sentence", rm.getMessage()).toString(), e1);
                    }
                }
            }
        }
    }

    private void dump() throws IOException {
        long t = getTimestampProvider().getNow();
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
            long bString = bytes;
            getLog().info(() -> getLogBuilder().wO("dump").wV("bytes", bString).wV("timestamp", new Date()).toString());
        }
    }
}
