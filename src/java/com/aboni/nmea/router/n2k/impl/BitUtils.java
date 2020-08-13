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
        private Res(long r, long max) {
            v = r;
            m = max;
        }

        public final long v;
        public final long m;
    }

    public static int getByte(byte[] data, int index) {
        return data[index] & 0xff;
    }

    public static long get4ByteInt(byte[] data, int index) {
        return ((long)getByte(data, index + 3) << 24) | (long)get3ByteInt(data, index);
    }

    public static int get3ByteInt(byte[] data, int index) {
        return (getByte(data, index + 2) << 16) | get2ByteInt(data, index);
    }

    public static int get2ByteInt(byte[] data, int index) {
        return (getByte(data, index + 1) << 8) | getByte(data, index);
    }

    public static Res extractBits(byte[] data, int startBit, int offset, int bits, boolean signed) {
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
        if (signed) {
            maxValue >>= 1;
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
        return new Res(value, maxValue);
    }
}

