package com.aboni.nmea.router.n2k.can;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CANReaderTest {

    private static class FrameListener implements CANFrameCallback {

        private byte[] lastRead;

        @Override
        public void onFrame(byte[] frame) {
            lastRead = frame;
        }

        public byte[] getLastRead() {
            return lastRead;
        }

        public void dump() {
            if (lastRead != null) {
                for (byte b : lastRead) System.out.printf("%02x ", b);
                System.out.println();
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
                for (byte b : lastRead) System.out.printf("%02x ", b);
                System.out.println(error);
            }
        }
    }

    private CANReader canReader;
    private FrameErrorListener e;
    private FrameListener l;

    @Before
    public void setUp() {
        canReader = new HL340USBSerialCANReader();
        l = new FrameListener();
        e = new FrameErrorListener();
        canReader.setFrameCallback(l);
        canReader.setErrCallback(e);
    }

    @Test
    public void testStartWithIncompleteFrame() {
        int[] buffer = new int[]{0x10, 0x10, 0x10, 0x55, 0x00, 0x00};
        int offset = 3;
        boolean managed = canReader.onRead(buffer, offset);
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
        boolean managed = canReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNotNull(l.getLastRead());
        assertNull(e.getLastRead());
        assertArrayEquals(new byte[]{
                (byte) 0xAA, (byte) 0xC8, (byte) 0xAB, (byte) 0xCD,
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08,
                (byte) 0x55
        }, l.getLastRead());

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
        boolean managed = canReader.onRead(buffer, offset);
        assertTrue(managed);
        assertNotNull(l.getLastRead());
        assertNull(e.getLastRead());
        assertArrayEquals(new byte[]{
                (byte) 0xAA, (byte) 0xC8, (byte) 0xAB, (byte) 0xCD,
                (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04,
                (byte) 0x05, (byte) 0x06, (byte) 0x07, (byte) 0x08, (byte) 0x09,
                (byte) 0x55
        }, l.getLastRead());
        l.dump();
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
        boolean managed = canReader.onRead(buffer, offset);
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
        boolean managed = canReader.onRead(buffer, offset);
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
        boolean managed = canReader.onRead(buffer, offset);
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
        boolean managed = canReader.onRead(buffer, offset);
        assertFalse(managed);
        assertNull(l.getLastRead());
        assertNull(e.getLastRead());
    }
}