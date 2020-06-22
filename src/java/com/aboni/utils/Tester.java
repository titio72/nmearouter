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

package com.aboni.utils;

import com.aboni.misc.Utils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tester {

    public interface TestingProc {
        boolean doIt(PrintStream out);

        boolean init(PrintStream out);
    }

    private final AtomicBoolean goon;
    private final int period;

    public Tester(int period) {
        goon = new AtomicBoolean();
        this.period = period;
    }

    private PrintStream getOut() {
        return ServerLog.getConsoleOut();
    }

    public void start(TestingProc runnable) {
        if (runnable.init(getOut())) {
            goon.set(true);
            Thread t = new Thread(() -> {
                try {
                    while (goon.get()) {
                        if (runnable.doIt(getOut())) {
                            Utils.pause(period);
                        } else {
                            break;
                        }
                    }
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.SEVERE, "Error", e);
                }
            });
            t.setDaemon(true);
            t.start();

            waitForEnterPressed();
            goon.set(false);
        }
    }

    private void waitForEnterPressed() {
        try {
            int ch;
            do {
                ch = System.in.read();
            } while (ch != 13);
        } catch (IOException e) {
            // do nothing
        }
    }
}
