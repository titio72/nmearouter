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

    public static class BitUtilsException extends RuntimeException {
        BitUtilsException(String msg) {
            super(msg);
        }
    }

    private BitUtils() {
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
            throw new BitUtilsException("End byte after data length");
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
                throw new BitUtilsException("Zero length integer");
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
            throw new BitUtilsException("End byte after data length");
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
