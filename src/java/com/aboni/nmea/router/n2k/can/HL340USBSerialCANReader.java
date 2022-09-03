package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHandler;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/*
------------------------------------------------------------------------------------------------------------------------
CAN Bus Data Frame
------------------------------------------------------------------------------------------------------------------------

The CAN Bus data frame format applies to both, send and receive.

Both message ID lengths, 11-bit standard and 29-bit extended, use the first two bytes in the serial data frame.
Depending on the message ID length, the remaining bytes will be filled differently.

The data frame does not utilize a checksum check, most probably due to keeping the number of transmitted bytes to a
minimum and cutting down on processing time.

Byte Description

0    0xAA = Packet Start

1    CAN Bus Data Frame Information

     Bit 7 Always 1

     Bit 6 Always 1

     Bit 5 0=STD; 1=EXT

     Bit 4 0=Data; 1=Remote


     Bit 3…0 Data Length Code (DLC); 0…8

Standard CAN data frame continues:

Byte Description

2    Message ID LSB

3    Message ID MSB

x    Data, length depending on DLC

y    0x55 = Packet End

Extended CAN data frame continues:

Byte Description

2    Message ID LSB

3    Message ID 2ND

4    Message ID 3RD

5    Message ID MSB

x    Data, length depending on DLC

y    0x55 = Packet End

CAN Bus Data Frame Size

Standard 11-bit    6 bytes with zero data bytes

                   14 bytes with eight data bytes

Extended 29-bit    8 bytes with zero data bytes

                   16 bytes with eight data bytes

------------------------------------------------------------------------------------------------------------------------
CAN Bus Initialization Frame
------------------------------------------------------------------------------------------------------------------------

The CAN Bus initialization frame has a constant length of 20 bytes.

Byte(s) Description

0       0xAA = Frame Start Byte 1

1       0x55 = Frame Start Byte 2

2       0x12 = Initialization Message ID

3       CAN Baud Rate (See table below)

4       0x01=STD; 0x02=EXT; Applies only to transmitting

5 – 8   Filter ID; LSB first, MSB last

9 – 12  Mask ID; LSB first, MSB last

13      Operation Mode (See table below)

14      0x01; Purpose Unknown

15 – 18 All 0x00

19      Checksum

Note: The checksum is processed between bytes 2 and 18.

CAN Bus baud rate

0x01 1000k

0x02 800k

0x03 500k

0x04 400k

0x05 250k

0x06 200k

0x07 125k

0x08 100k

0x09 50k

0x0A 20k

0x0B 10k

0x0C 5k

Operation Mode

0x00 Normal

0x01 Loopback

0x02 Silent (Listen-Only)

0x03 Loopback + Silent

------------------------------------------------------------------------------------------------------------------------
CAN Controller Status Frame
------------------------------------------------------------------------------------------------------------------------

The format of the status request and status report frames are identical, where the request frame sends all zeroes
between the frame header (frame start tokens plus frame ID) and the checksum, i.e. bytes 3 through 18 are 0x00.

Byte(s) Description

0       0xAA = Frame Start Byte 1

1       0x55 = Frame Start Byte 2

2       0x04 = Status Message ID

3       Receive Error Counter

4       Transmit Error Counter

5 – 18  Unknown Purpose
        In case of status report, they may be filled with data

19 Checksum
 */

public class HL340USBSerialCANReader implements SerialCANReader {

    private N2KMessageHandler callback;
    private CANErrorCallback errCallback;
    private CANFrameCallback frameCallback;
    private final N2KMessageFactory msgFactory;
    private final CANReaderStats stats = new CANReaderStats();

    @Inject
    public HL340USBSerialCANReader(@NotNull N2KMessageFactory msgFactory) {
        this.msgFactory = msgFactory;
    }

    @Override
    public void setFrameCallback(CANFrameCallback callback) {
        this.frameCallback = callback;
    }

    @Override
    public void setCallback(N2KMessageHandler callback) {
        this.callback = callback;
    }

    @Override
    public void setErrCallback(CANErrorCallback errCallback) {
        this.errCallback = errCallback;
    }

    @Override
    public boolean onRead(int[] b, int lastByteOffset) {
        if (lastByteOffset >= 1 && isEndOfFrame(b[lastByteOffset]) && !isStartOfFrame(b[0])) {
            // ignore, we are missing the first part of buffer
            stats.incrementInvalidFrames();
            if (errCallback != null) {
                errCallback.onError(getBytes(b, lastByteOffset + 1), "Incomplete frame: missing start");
            }
            return true;
        } else if (lastByteOffset > 2 && /* skip upfront the case where too few bytes have been received to be something meaningful */
                isStartOfFrame(b[0]) && isEndOfFrame(b[lastByteOffset]) && /* starts well and ends well */
                getTotalFrameSize(b[1]) <= (lastByteOffset + 1) /* the buffer is long enough, a "terminator" 0x55 may be in the data buffer */) {
            // regular frame
            if (isDataPackage(b[1]) && !isRemote(b[1])) {
                // good data frame
                handleFrame(b, lastByteOffset + 1 /* compatibility */);
            } else {
                stats.incrementOtherFrames();
            }
            return true;
        } else if (lastByteOffset > 2 && isStartOfFrame(b[0]) && isEndOfFrame(b[1]) &&
                (b[2] == 0x12 /* configuration frame */ || b[2] == 0x04 /* command frame */) &&
                lastByteOffset == 19 /* always 20 bytes long */) {
            stats.incrementOtherFrames();
            return true;
        }
        return false;
    }

    @Override
    public CANReaderStats getStats() {
        return stats;
    }

    private static int getTotalFrameSize(int b) {
        int dataSize = getDataSize(b);
        return isExt(b) ?
                1 + 1 + 4 + dataSize + 1 :
                1 + 1 + 2 + dataSize + 1;
    }

    private static int getDataSize(int b) {
        return b & 0x0F;
    }

    private static boolean isStartOfFrame(int b) {
        return b == 0xaa;
    }

    private static boolean isEndOfFrame(int b) {
        return b == 0x55;
    }

    private static boolean isDataPackage(int type) {
        return ((type >> 6) ^ 3) == 0; // checks the 6th and 7th bit to be 1
    }

    private static boolean isRemote(int type) {
        return (type & 0x10) != 0; // checks the 4th bit
    }

    private static boolean isExt(int type) {
        return (type & 0x20) != 0; // check the 5th bit
    }

    private static long getExtId(int[] b) {
        return b[2] + ((long) b[3] << 8) + ((long) b[4] << 16) + ((long) b[5] << 24);
    }

    private static long getId(int[] b) {
        return b[2] + ((long) b[3] << 8);
    }

    private static int getFirstDataByteIndex(boolean ext) {
        return ext ? 6 : 4;
    }

    private static boolean checkBufferSize(int l, int dataSize, boolean ext) {
        // 3 because we have the initial 0xaa, the final 0x55 and the type (first byte after the 0xaa)
        return l >= (3 + dataSize + (ext ? 4 : 2));
    }

    private void handleFrame(int[] b, int length) {
        int dataSize = getDataSize(b[1]);
        boolean ext = isExt(b[1]);
        if (checkBufferSize(length, dataSize, ext)) {
            stats.incrementDataFrames();
            long id = ext ? getExtId(b) : getId(b);
            CANDataFrame frame = getFrame(b, dataSize, ext, id);
            dumpAnalyzerFormat(frame);
        } else {
            stats.incrementInvalidFrames();
            if (errCallback != null) {
                byte[] errB = getBytes(b, length);
                errCallback.onError(errB, "Wrong buffer size");
            }
        }
    }

    private byte[] getBytes(int[] b, int length) {
        byte[] errB = new byte[length];
        for (int i = 0; i < length; i++) {
            errB[i] = (byte) (b[i] & 0xFF);
        }
        return errB;
    }

    private static CANDataFrame getFrame(int[] b, int dataSize, boolean ext, long id) {
        byte[] data = new byte[dataSize];
        int dataStart = getFirstDataByteIndex(ext);
        for (int i = 0; i < dataSize; i++) data[i] = (byte) (b[dataStart + i] & 0xFF);
        return CANDataFrame.create(id, data);
    }

    private void dumpAnalyzerFormat(CANDataFrame frame) {

        if (frameCallback != null) {
            frameCallback.onFrame(frame);
        }
        if (callback != null) {
            N2KHeader iso = new N2KHeader(frame.getId());
            N2KMessage msg = msgFactory.newUntypedInstance(iso, frame.getData());
            callback.onMessage(msg);
        }
    }

    public static String dumpCanDumpFormat(int[] b, int dataSize, long id) {
        StringBuilder res = new StringBuilder(String.format("  %08x  [%d]", id, dataSize));
        for (int i = 0; i < dataSize; i++)
            res.append(String.format(" %02x", b[i]));

        return res.toString();
    }
}
