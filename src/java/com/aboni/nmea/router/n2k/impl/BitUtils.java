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

package com.aboni.nmea.router.n2k.impl;

public class BitUtils {

    private BitUtils() {
    }

    public static class Res {

        private Res(long[] r) {
            v = r[0];
            m = r[1];
        }

        public final long v;
        public final long m;
    }

    public static Res extractBits(byte[] data, int start, int off, int len, boolean signed) {
        //long[] res = new long[]{extractBits(data, off, len, signed), 0};
        long[] res = extractNumber(data, off, start, len, signed);
        return new Res(res);
    }

    public static long[] extractNumber(byte[] data, int offset, int startBit, int bits, boolean hasSign) {
        long value = 0;
        long maxValue = 0;

        int dataIndex = offset / 8;

        int firstBit = startBit;
        int bitsRemaining = bits;
        int magnitude = 0;
        int bitsInThisByte;
        long bitMask;
        long allOnes;
        long valueInThisByte;


        while (bitsRemaining > 0) {
            bitsInThisByte = Math.min(8 - firstBit, bitsRemaining);
            allOnes = ((1L << bitsInThisByte) - 1);

            // How are bits ordered in bytes for bit fields? There are two ways, first field at LSB or first
            // field as MSB.
            // Experimentation, using the 129026 PGN, has shown that the most likely candidate is LSB.
            bitMask = allOnes << firstBit;
            valueInThisByte = (data[dataIndex] & bitMask) >> firstBit;

            value |= valueInThisByte << magnitude;
            maxValue |= allOnes << magnitude;

            magnitude += bitsInThisByte;
            bitsRemaining -= bitsInThisByte;
            firstBit += bitsInThisByte;
            if (firstBit >= 8) {
                firstBit -= 8;
                dataIndex++;
            }
        }
        if (hasSign) {
            maxValue >>= 1;
            /*if (offset>0) { // J1939 Excess-K notation
                value += offset;
            } else */
            {
                // check if the first bit is 1
                boolean negative = (value & (1L << (bits - 1))) > 0;

                if (negative) {
                    /* Sign extend value for cases where bits < 64 */
                    /* Assume we have bits = 16 and value = -2 then we do: */
                    /* 0000.0000.0000.0000.0111.1111.1111.1101 value    */
                    /* 0000.0000.0000.0000.0111.1111.1111.1111 maxvalue */
                    /* 1111.1111.1111.1111.1000.0000.0000.0000 ~maxvalue */
                    value |= ~maxValue;
                }
            }
        }
        return new long[]{value, maxValue};
    }

    private static int uByte(byte val) {
        return (int) val & 0xff;
    }

    private static int extractShort(byte[] data, int offset) {
        return (uByte(data[offset + 1]) * 256) + uByte(data[offset]);
    }

    private static int extractInt(byte[] data, int offset, int nBits) {
        return (int) extractLong(data, offset, nBits);
    }

    private static long extractLong(byte[] data, int offset, int nBytes) {
        long val = 0;
        for (int i = 0; i < nBytes; i++) {
            val += ((long) uByte(data[offset + i])) << (8 * i);
        }
        return val;
    }

    private static int extractInt(byte[] data, int offset) {
        int res = uByte(data[offset + 3]);
        for (int i = 2; i >= 0; i--) res = res << 8 | uByte(data[offset + i]);
        return res;
    }

    public static long extractBits(byte[] data, int off, int len, boolean signed) {
        if ((off % 8) == 0 && (len % 8) == 0) {
            return extractEntireBytes(data, off / 8, len / 8, signed);
        } else {
            return extractPartialBytes(data, off, len, signed);
        }
    }

    private static final long[] MASKS = new long[]{
            0x00L, //not used
            0x00ffL,
            0x00ffffL,
            0x00ffffffL,
            0x00ffffffffL
    };

    private static long extractPartialBytes(byte[] data, int off, int len, boolean signed) {
        long res;
        int offB = off / 8;
        int endOffB = (off + len + 7) / 8;
        if (endOffB > data.length) {
            return 0x7fffffffffffffffL;
        }
        int lenB = endOffB - offB;
        switch (lenB) {
            case 1:
                res = data[offB];
                res = res & MASKS[lenB];
                break;
            case 2:
                res = BitUtils.extractShort(data, offB);
                res = res & MASKS[lenB];
                break;
            case 3:
                res = BitUtils.extractInt(data, offB, 3);
                res = res & MASKS[lenB];
                break;
            case 4:
                res = BitUtils.extractInt(data, offB);
                break;
            default:
                return 0x7fffffffffffffffL;
        }
        int shift = off % 8;
        int maskShift = 32 - len;
        res = res >> shift;
        long mask = MASKS[4] >> maskShift;
        res = res & mask;
        if (signed) {
            res = (res << (32 - len)) >> (32 - len);
        }
        return res;
    }

    private static long extractEntireBytes(byte[] data, int offB, int lenB, boolean signed) {
        long res;
        if (lenB > 8) {
            return 0x7fffffffffffffffL;
        }
        switch (lenB) {
            case 1:
                res = data[offB];
                if (!signed) res &= MASKS[lenB];
                break;
            case 2:
                res = BitUtils.extractShort(data, offB);
                if (!signed) res &= MASKS[lenB];
                break;
            case 4:
                res = BitUtils.extractInt(data, offB);
                if (!signed) res &= MASKS[lenB];
                break;
            default:
                res = BitUtils.extractLong(data, offB, lenB);
                if ((!signed) && (lenB < 8)) res = res & (0x7fffffffffffffffL >> (63 - (lenB * 8)));
                break;
        }
        return res;
    }
}



