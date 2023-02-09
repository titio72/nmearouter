package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.nmea.router.n2k.messages.impl.N2KGenericMessageImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SerialHL340SerialCANReaderTest {

    private static class FrameListener implements CANFrameCallback {

        private CANDataFrame lastRead;

        @Override
        public void onFrame(CANDataFrame frame) {
            lastRead = frame;
        }

        public CANDataFrame getLastRead() {
            return lastRead;
        }

        public void dump() {
            if (lastRead != null) {
                System.out.println(lastRead.toString());
            }
        }
    }

    private static class FrameErrorListener implements CANErrorCallback {

        private byte[] lastRead;
        private String error;

        @Override
        public void onError(byte[] frame, String errorMessage) {
            lastRead = frame;
            error = errorMessage;
        }

        public String getError() {
            return error;
        }

        public byte[] getLastRead() {
            return lastRead;
        }

        public void dump() {
            if (lastRead != null) {
                System.out.println(lastRead);
            }
        }
    }

    private SerialCANReader serialCanReader;
    private FrameErrorListener e;
    private FrameListener l;

    @Before
    public void setUp() {
        serialCanReader = new HL340USBSerialCANReader(new N2KMessageFactory() {
            @Override
            public boolean isSupported(int pgn) {
                return false;
            }

            @Override
            public boolean isFast(int pgn) {
                return false;
            }

            @Override
            public N2KMessage newUntypedInstance(N2KMessageHeader h, byte[] data) {
                return new N2KGenericMessageImpl(h, data);
            }

            @Override
            public N2KMessage newInstance(N2KMessageHeader h, byte[] data) throws PGNDataParseException {
                return null;
            }

            @Override
            public N2KMessage newInstance(int pgn, byte[] data) throws PGNDataParseException {
                return null;
            }
        });
        l = new FrameListener();
        e = new FrameErrorListener();
        serialCanReader.setFrameCallback(l);
        serialCanReader.setErrCallback(e);
    }

    @Test
    public void testStartWithIncompleteFrame() {
        int[] buffer = new int[]{0x10, 0x10, 0x10, 0x55, 0x00, 0x00};
        int offset = 3;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNull(l.getLastRead());
        assertNotNull(e.getLastRead());
        e.dump();
    }

    @Test
    public void testGoodFrame() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0xC8, // data frame, 8 bytes long
                0xAB, 0xCD, // id
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x55, // end of frame
                0x00, 0x00 // unused tail
        };
        int offset = 12;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNotNull(l.getLastRead());
        assertNull(e.getLastRead());
        assertEquals(CANDataFrame.create(0xCDAB, new byte[]{
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08}), l.getLastRead());

        l.dump();
    }

    @Test
    public void testGoodFrameButLongerThanExpected() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0xC8, // data frame, 8 bytes long
                0xAB, 0xCD, // id
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, // data
                0x09, // extra data
                0x55, // end of frame
                0x00, 0x00 // unused tail
        };
        int offset = 13;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNotNull(l.getLastRead());
        assertNull(e.getLastRead());
        assertEquals(CANDataFrame.create(0xCDAB, new byte[]{
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08}), l.getLastRead());
    }

    @Test
    public void testGoodFrameIncomplete() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0xC8, // data frame, 8 bytes long
                0xAB, 0xCD, // id
                0x01, 0x02, 0x03, 0x04
        };
        int offset = 7;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertFalse(managed);
        assertNull(l.getLastRead());
        assertNull(e.getLastRead());
    }

    @Test
    public void testIncompleteFrameWithTerminatorInData() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0xC8, // data frame, 8 bytes long
                0xAB, 0xCD, // id
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x55, // data - last byte is equal to a terminator but it's not meant to be a terminator
                0x00, 0x00 // unused tail (not filled yet...)
        };
        int offset = 11;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertFalse(managed);
        assertNull(l.getLastRead());
        assertNull(e.getLastRead());
    }

    @Test
    public void testOtherFrameComplete1() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0x55, // control frame
                0x12, // configuration frame
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0xCC // checksum
        };
        int offset = 19;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNull(l.getLastRead());
        assertNull(e.getLastRead());
    }

    @Test
    public void testOtherFrameIncomplete1() {
        int[] buffer = new int[]{
                0xAA, // start of frame
                0x55, // control frame
                0x12, // configuration frame
                0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
        };
        int offset = 10;
        boolean managed = serialCanReader.onRead(buffer, offset);
        assertFalse(managed);
        assertNull(l.getLastRead());
        assertNull(e.getLastRead());
    }
}