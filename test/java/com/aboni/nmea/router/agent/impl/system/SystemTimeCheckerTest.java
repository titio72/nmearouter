package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.message.MsgHeading;
import com.aboni.nmea.router.message.MsgPosition;
import com.aboni.nmea.router.message.MsgSOGAdCOG;
import com.aboni.utils.ConsoleLog;
import com.aboni.utils.DataEvent;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemTimeCheckerTest {

    private static class NMEACacheMock implements NMEACache {

        @Override
        public DataEvent<MsgHeading> getLastHeading() {
            return null;
        }

        @Override
        public DataEvent<MsgPosition> getLastPosition() {
            return null;
        }

        @Override
        public DataEvent<MsgSOGAdCOG> getLastVector() {
            return null;
        }

        @Override
        public boolean isHeadingOlderThan(long time, long threshold) {
            return false;
        }

        @Override
        public void onSentence(Message s, String src) {
        }

        @Override
        public <T> void setStatus(String statusKey, T status) {
        }

        @Override
        public <T> T getStatus(String statusKey, T defaultValue) {
            return null;
        }

    }

    private static class MyTimestampProvider implements TimestampProvider {

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
        MyTimestampProvider tp = new MyTimestampProvider();
        AtomicBoolean attemptedChanged = new AtomicBoolean(false);
        SystemTimeChecker checker = new SystemTimeChecker(cache, tp, timestamp -> {
            tp.setNow(timestamp.toEpochMilli());
            attemptedChanged.set(true);
        }, ConsoleLog.getLogger());

        // the sentence is 1 second behind the system time - expected to be considered ok
        OffsetDateTime t = OffsetDateTime.parse("2020-02-03T10:30:21+00:00");
        tp.setNow(now.toEpochSecond() * 1000);
        checker.checkAndSetTime(t.toInstant());
        assertTrue(checker.isSynced()); // check if synced
        assertFalse(attemptedChanged.get()); // check that an attempt to change the system date was NOT made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

    @Test
    public void checkAndSetTime_TimeWasNotOk() {
        OffsetDateTime now = OffsetDateTime.parse("2020-02-03T10:30:22+00:00");
        NMEACacheMock cache = new NMEACacheMock();
        MyTimestampProvider tp = new MyTimestampProvider();
        AtomicBoolean attemptedChanged = new AtomicBoolean(false);
        SystemTimeChecker checker = new SystemTimeChecker(cache, tp, timestamp -> {
            tp.setNow(timestamp.toEpochMilli());
            attemptedChanged.set(true);
        }, ConsoleLog.getLogger());

        // the sentence is 10 minutes ahead of the system time - the checker should try to change the system time
        OffsetDateTime t = OffsetDateTime.parse("2020-02-03T10:40:21+00:00");
        tp.setNow(now.toEpochSecond() * 1000);
        checker.checkAndSetTime(t.toInstant());
        assertTrue(checker.isSynced()); // check if synced
        assertTrue(attemptedChanged.get()); // check that an attempt to change the system date was made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

}