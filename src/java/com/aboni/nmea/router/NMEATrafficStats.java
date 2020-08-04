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

package com.aboni.nmea.router;

public class NMEATrafficStats {

    private static final long DEFAULT_TIMER_COUNT = 30;

    public interface StatsOnTimer {
        void onStats(NMEATrafficStats stats, long timestamp);
    }

    public static class Stats {

        long bytes = 0;
        long sentences = 0;
        long sentenceErrs = 0;

        public long getBytes() {
            return bytes;
        }

        public long getSentences() {
            return sentences;
        }

        public long getSentenceErrs() {
            return sentenceErrs;
        }

        private void reset() {
            bytes = 0;
            sentenceErrs = 0;
            sentences = 0;
        }

        public String toString(long period) {
            return String.format("Bytes {%d} bps {%.0f} Msg {%d/%d}",
                    bytes, (bytes * 8 * 1000.0) / period, sentences, sentences + sentenceErrs);
        }
    }

    private boolean input;
    private boolean output;
    private final StatsOnTimer callback;
    private long expireAfterTimerCount;
    private final Stats statsIn;
    private final Stats statsOut;
    private long timerCount;
    private long time0;

    public NMEATrafficStats(StatsOnTimer callback, boolean input, boolean output) {
        this(callback, DEFAULT_TIMER_COUNT, input, output);
    }

    public NMEATrafficStats(StatsOnTimer callback) {
        this.callback = callback;
        statsOut = new Stats();
        statsIn = new Stats();
        setup(1, true, true);
    }

    public NMEATrafficStats(StatsOnTimer callback, long expireAfterTimerCount, boolean input, boolean output) {
        this.callback = callback;
        statsOut = new Stats();
        statsIn = new Stats();
        setup(expireAfterTimerCount, input, output);
    }

    public void setup(long expireAfterTimerCount, boolean input, boolean output) {
        this.expireAfterTimerCount = expireAfterTimerCount;
        this.input = input;
        this.output = output;
        timerCount = 0;
    }

    public Stats getStatsIn() {
        return statsIn;
    }

    public Stats getStatsOut() {
        return statsOut;
    }

    public void updateReadStats(String s) {
        if (input) {
            synchronized (statsIn) {
                int l = s.length() + 2;
                statsIn.bytes += l;
            }
        }
    }

    public void updateReadStats(boolean fail) {
        if (input) {
            synchronized (statsIn) {
                if (fail) {
                    statsIn.sentenceErrs++;
                } else {
                    statsIn.sentences++;
                }
            }
        }
    }

    public void updateWriteStats(String s) {
        if (output) {
            synchronized (statsOut) {
                int l = s.length() + 2;
                statsOut.bytes += l;
            }
        }
    }

    public void updateWriteStats(boolean fail) {
        if (output) {
            synchronized (statsOut) {
                if (fail)
                    statsOut.sentenceErrs++;
                else
                    statsOut.sentences++;
            }
        }
    }

    public void onTimer(long time) {
        timerCount++;
        if (timerCount > expireAfterTimerCount) {
            if (callback != null) {
                callback.onStats(this, time);
            }
            if (input) statsIn.reset();
            if (output) statsOut.reset();
            timerCount = 0;
            time0 = time;
        }
    }

    public String toString(long time) {
        long period = time - time0;
        if (input && output) {
            return String.format("In {%s} Out {%s}", statsIn.toString(period), statsOut.toString(period));
        } else if (input) {
            return String.format("In {%s}", statsIn.toString(period));
        } else if (output) {
            return String.format("Out {%s}", statsOut.toString(period));
        } else {
            return "none";
        }
    }
}
