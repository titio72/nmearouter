package com.aboni.nmea.router.agent.impl.system;

import com.aboni.nmea.router.utils.ConsoleLog;
import com.aboni.nmea.router.utils.ProgrammableTimeStampProvider;
import org.junit.Before;
import org.junit.Test;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SystemTimeCheckerTest {

    private final ProgrammableTimeStampProvider tp = new ProgrammableTimeStampProvider();
    private final AtomicBoolean attemptedChanged = new AtomicBoolean(false);
    private SystemTimeChecker checker;

    @Before
    public void setup() {
        OffsetDateTime now = OffsetDateTime.parse("2020-02-03T10:30:22+00:00");
        tp.setTimestamp(now.toInstant().toEpochMilli());
        attemptedChanged.set(false);
        checker = new SystemTimeChecker(tp, timestamp -> {
            tp.setTimestamp(timestamp.toEpochMilli());
            attemptedChanged.set(true);
        }, ConsoleLog.getLogger());
    }

    @Test
    public void checkAndSetTime_TimeWasOkAlready() {
        // the sentence is 1 second behind the system time - expected to be considered ok
        OffsetDateTime gpsTime = OffsetDateTime.parse("2020-02-03T10:30:21+00:00");
        checker.checkAndSetTime(gpsTime.toInstant());

        assertTrue(checker.isSynced()); // check if synced
        assertFalse(attemptedChanged.get()); // check that an attempt to change the system date was NOT made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

    @Test
    public void checkAndSetTime_TimeWasNotOk() {
        // the sentence is 10 minutes ahead of the system time - the checker should try to change the system time
        OffsetDateTime t = OffsetDateTime.parse("2020-02-03T10:40:21+00:00");
        checker.checkAndSetTime(t.toInstant());

        assertTrue(checker.isSynced()); // check if synced
        assertTrue(attemptedChanged.get()); // check that an attempt to change the system date was made
        assertTrue(checker.getTimeSkew() < SystemTimeChecker.TOLERANCE_MS); // check skew
    }

}