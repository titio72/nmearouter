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

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.utils.Log;
import com.aboni.nmea.sentences.NMEASentenceItem;
import com.aboni.utils.Utils;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileReader;

public class NMEAPlayer extends NMEAAgentImpl {

    private String file;

    @Inject
    public NMEAPlayer(Log log, TimestampProvider tp) {
        super(log, tp, true, false);
    }

    @Override
    public String getDescription() {
        return "File " + getFile();
    }

    @Override
    public String getType() {
        return "NMEA log player";
    }

    @Override
    public String toString() {
        return getType();
    }


    public void setFile(String file) {
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    @Override
    public void onDeactivate() {
        if (isStarted() && !stop)
            stop = true;
    }

    @Override
    public boolean onActivate() {
        if (file!=null) {
            Thread t = new Thread(this::go, "NMEA Player [" + getName() + "]");
            t.setDaemon(true);
            t.start();
            return true;
        } else {
            return false;
        }
    }

    private boolean stop = false;

    private void go() {
        while (!stop) {
            try (FileReader fr = new FileReader(getFile())) {
                try (BufferedReader r = new BufferedReader(fr)) {
                    String line;
                    long logT0 = 0;
                    long t0 = 0;
                    while ((line = r.readLine()) != null) {
                        if (line.startsWith("[")) {
                            long logT = readLineWithTimestamp(line, logT0, t0);
                            t0 = getTimestampProvider().getNow();
                            logT0 = logT;
                        } else {
                            readLine(line);
                        }
                    }
                }
            } catch (Exception e) {
                getLog().error(() -> getLogBuilder().wO("play").toString(), e);
                Utils.pause(10000);
            }
        }
        stop = false;
    }

    private void readLine(String line) {
        try {
            Sentence s = SentenceFactory.getInstance().createParser(line);
            Thread.sleep(55);
            postMessage(s);
        } catch (InterruptedException e) {
            getLog().error(() -> getLogBuilder().wO("play").wV("line", line).toString(), e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().wO("play").wV("line", line).toString(), e);
        }
    }

    private long readLineWithTimestamp(String line, long logT0, long t0) {
        long logT = logT0;
        try {
            NMEASentenceItem itm = new NMEASentenceItem(line);
            long t = getTimestampProvider().getNow();
            logT = itm.getTimestamp();
            long dt = t - t0;
            long dLogT = logT - logT0;
            if (dLogT > dt) {
                Utils.pause((int) (dLogT - dt));
            }
            postMessage(itm.getSentence());
        } catch (Exception e) {
            getLog().error(() -> getLogBuilder().wO("play").wV("line", line).toString(), e);
        }
        return logT;
    }
}
