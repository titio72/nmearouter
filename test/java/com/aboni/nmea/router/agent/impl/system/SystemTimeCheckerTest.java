package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.utils.DataEvent;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemTimeCheckerTest {

    private class NMEACacheMock implements NMEACache {

        @Override
        public DataEvent<HeadingSentence> getLastHeading() {
            return null;
        }

        @Override
        public DataEvent<PositionSentence> getLastPosition() {
            return null;
        }

        @Override
        public boolean isHeadingOlderThan(long time, long threshold) {
            return false;
        }

        @Override
        public void onSentence(Sentence s, String src) {
        }

        @Override
        public <T> void setStatus(String statusKey, T status) {
        }

        @Override
        public <T> T getStatus(String statusKey, T defaultValue) {
            return null;
        }

        @Override
        public long getNow() {
            return now;
        }

        void setNow(long l) {
            now = l;
        }

        private long now;
    }

    @Test
    public void checkAndSetTime_TimeWasOkAlready() {
        OffsetDateTime now = OffsetDateTime.parse("2020-02-03T10:30:22+00:00");
        NMEACacheMock cache = new NMEACacheMock();
        AtomicBoolean attemptedChanged = new AtomicBoolean(false);
        SystemTimeChecker checker = new SystemTimeChecker(cache, timestamp -> {
            cache.setNow(timestamp.toEpochSecond() * 1000);
            attemptedChanged.set(true);
        });

        // the sentence is 1 second behind the system time - expected to be considered ok
        Sentence s = SentenceFactory.getInstance().createParser("$IIRMC,103021.00,A,5046.305,N,00132.959,W,5.30,107.3,030220,0.9,W,A");
        cache.setNow(now.toEpochSecond() * 1000);
        checker.checkAndSetTime(s);
        assertTrue(checker.isSynced()); // check if synced
        assertFalse(attemptedChanged.get()); // check that an attempt to change the system date was NOT made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

    @Test
    public void checkAndSetTime_TimeWasNotOk() {
        OffsetDateTime now = OffsetDateTime.parse("2020-02-03T10:30:22+00:00");
        NMEACacheMock cache = new NMEACacheMock();
        AtomicBoolean attemptedChanged = new AtomicBoolean(false);
        SystemTimeChecker checker = new SystemTimeChecker(cache, timestamp -> {
            cache.setNow(timestamp.toEpochSecond() * 1000);
            attemptedChanged.set(true);
        });

        // the sentence is 10 minutes ahead of the system time - the checker should try to change the system time
        Sentence s = SentenceFactory.getInstance().createParser("$IIRMC,104021.00,A,5046.305,N,00132.959,W,5.30,107.3,030220,0.9,W,A");
        cache.setNow(now.toEpochSecond() * 1000);
        checker.checkAndSetTime(s);
        assertTrue(checker.isSynced()); // check if synced
        assertTrue(attemptedChanged.get()); // check that an attempt to change the system date was made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

}