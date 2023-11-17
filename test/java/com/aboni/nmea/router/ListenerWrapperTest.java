package com.aboni.nmea.router;

import com.aboni.log.ConsoleLog;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.router.ListenerWrapper;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ListenerWrapperTest {

    private static class MyListener {

        List<RouterMessage> received = new ArrayList<>();

        @OnRouterMessage
        public void onMessage(RouterMessage message) {
            received.add(message);
        }
    }

    private static class MyInvalidListener {

        @OnRouterMessage
        public void onMessage(RouterMessage message, String extraParameter) {
        }
    }

    private static class MyExceptionListener {

        @OnRouterMessage
        public void onMessage(RouterMessage message) {
            throw new RuntimeException("BOOOOM!");
        }
    }

    private static class MyMessage implements RouterMessage {

        private final long t;
        private final String s;

        public MyMessage(long t, String s) {
            this.t = t;
            this.s = s;
        }

        @Override
        public long getTimestamp() {
            return t;
        }

        @Override
        public String getAgentSource() {
            return s;
        }

        @Override
        public Message getPayload() {
            return null;
        }
    }

    @Test
    public void testListenerWrapper() throws ListenerWrapper.MessageDispatchException {
        MyListener l = new MyListener();
        ListenerWrapper lw = new ListenerWrapper(l, ConsoleLog.getLogger());
        lw.dispatch(new MyMessage(1, "M1"));
        assertEquals(1, l.received.size());
        assertEquals(1, l.received.get(0).getTimestamp());
    }

    @Test
    public void testListenerWrapperException() {
        MyExceptionListener l = new MyExceptionListener();
        ListenerWrapper lw = new ListenerWrapper(l, ConsoleLog.getLogger());
        assertThrows(ListenerWrapper.MessageDispatchException.class, ()->lw.dispatch(new MyMessage(1, "M1")));
    }

    @Test
    public void testListenerWrapperInvalid() {
        MyInvalidListener l = new MyInvalidListener();
        assertThrows(RuntimeException.class, () -> new ListenerWrapper(l, ConsoleLog.getLogger()));
    }

    @Test
    public void testListenerWrapperNoAnnotation() {
        Object l = new Object();
        assertThrows(RuntimeException.class, () -> new ListenerWrapper(l, ConsoleLog.getLogger()));
    }


}