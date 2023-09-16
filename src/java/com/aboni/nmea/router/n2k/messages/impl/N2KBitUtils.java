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

package com.aboni.nmea.router.n2k.messages.impl;

import java.util.Map;

public class N2KBitUtils {

    private N2KBitUtils() {
    }

    public static int getByte(byte[] data, int ix, int def) {
        return (ix < data.length) ? data[ix] & 0xFF : def;
    }

    public static String parseEnum(byte[] data, int offset, int start, int length, Map<Integer, String> map) {
        if (offset + length > (data.length * 8)) return null;
        Res e = extractBits(data, start, offset, length, false);
        return map.getOrDefault((int) e.v, String.format("%d", e.v));
    }

    public static long parseIntegerSafe(byte[] data, int offset, int start, int length, long def) {
        if (offset + length > (data.length * 8)) return def;

        Res e = extractBits(data, start, offset, length, false);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATA FIELD_ERROR and DATA FIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATA FIELD_UNKNOWN */
        }
        if (e.v <= e.m - reserved) {
            return e.v;
        } else {
            return def;
        }
    }

    public static Long parseInteger(byte[] data, int offset, int length) {
        if (offset + length > (data.length * 8)) return null;

        Res e = extractBits(data, 0, offset, length, false);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATA FIELD_ERROR and DATA FIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATA FIELD_UNKNOWN */
        }
        if (e.v <= e.m - reserved) {
            return e.v;
        } else {
            return null;
        }
    }

    public static double parseDoubleSafe(byte[] data, int offset, int length, double precision, boolean signed) {
        Double d = parseDouble(data, offset, length, precision, signed);
        return d == null ? Double.NaN : d;
    }

    public static Double parseDouble(byte[] data, int offset, int length, double precision, boolean signed) {
        if (offset + length > (data.length * 8)) return null;

        Res e = extractBits(data, 0, offset, length, signed);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATA FIELD_ERROR and DATA FIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATA FIELD_UNKNOWN */
        }
        if (length == 64) {
            if (e.v != 0x7FFFFFFFFFFFFFFFL)
                return e.v * precision;
            else
                return null;
        } else {
            if (e.v <= e.m - reserved) {
                return e.v * precision;
            } else {
                return null;
            }
        }
    }

    public static String getText(byte[] data, int byteStart, int byteLength) {

        if (byteStart < 0 || byteStart >= data.length) return "";

        byteLength = Math.min(byteLength, data.length - byteStart);

        // remove padding
        int l = byteLength;
        byte last = data[byteStart + l - 1];
        while (last == (byte) 0xff || last == ' ' || last == 0 || last == '@') {
            l--;
            if (l == 0) return "";
            last = data[byteStart + l - 1];
        }
        char c;
        int k;

        StringBuilder sb = new StringBuilder(l);
        // escape
        for (k = 0; k < l; k++) {
            c = (char) data[byteStart + k];
            switch (c) {
                case '\b':
                    sb.append("\\b");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    if (c >= ' ' && c <= '~') {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    public static boolean isValidDouble(double d) {
        return !Double.isNaN(d);
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
        return ((long) getByte(data, index + 3) << 24) | get3ByteInt(data, index);
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

