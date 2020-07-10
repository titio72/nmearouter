package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.BitUtils;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.Instant;
import java.util.Map;

public abstract class N2KMessageImpl implements N2KMessage {

    protected N2KMessageHeader header;
    protected byte[] data;

    protected boolean isValidByte(int b) {
        return b != 0xFF;
    }

    protected boolean isValidDouble(double d) {
        return !Double.isNaN(d);
    }

    private static class DefaultHeader implements N2KMessageHeader {
        int pgn;
        Instant now;

        DefaultHeader(int pgn, Instant now) {
            this.pgn = pgn;
            this.now = now;
        }

        @Override
        public int getPgn() {
            return pgn;
        }

        @Override
        public int getSource() {
            return 0;
        }

        @Override
        public int getDest() {
            return 255;
        }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public Instant getTimestamp() {
            return now;
        }
    }

    protected static N2KMessageHeader getDefaultHeader(int pgn) {
        return new DefaultHeader(pgn, Instant.now());
    }

    protected N2KMessageImpl(N2KMessageHeader header, byte[] data) {
        this.data = data;
        this.header = header;
    }

    @Override
    public N2KMessageHeader getHeader() {
        return header;
    }

    protected static int getByte(byte[] data, int ix, int def) {
        if (ix < data.length)
            return data[ix] & 0xFF;
        else
            return def;
    }

    protected static String parseAscii(byte[] data, int offset, int start, int length) {

        int totBits = data.length * 8;
        if (offset < totBits && (offset + length) > totBits && (totBits - offset) >= 8) length = totBits - offset;
        if (offset + length > totBits) return null;


        byte[] b = new byte[(int) Math.ceil(length / 8.0)];
        for (int i = 0; i < length; i += 8) {
            long e = BitUtils.extractBits(data, start, offset + i, 8, false).v;
            b[i / 8] = (byte) e;
        }
        return getASCII(b);
    }

    protected static String parseEnum(byte[] data, int offset, int start, int length, Map<Integer, String> map) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, false);
        int reserved = 0;
        if (e.m >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }

        String ret = null;
        if (e.v <= (e.m - reserved)) {
            ret = map.getOrDefault((int) e.v, String.format("%d", e.v));
        }
        return ret;
    }

    protected static long parseIntegerSafe(byte[] data, int offset, int start, int length, boolean signed, long def) {
        if (offset + length > (data.length * 8)) return def;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, signed);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }
        if (e.v <= e.m - reserved) {
            return e.v;
        } else {
            return def;
        }
    }

    protected static Long parseInteger(byte[] data, int offset, int start, int length, boolean signed) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, signed);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }
        if (e.v <= e.m - reserved) {
            return e.v;
        } else {
            return null;
        }
    }

    protected static double parseDoubleSafe(byte[] data, int offset, int start, int length, double precision, boolean signed) {
        Double d = parseDouble(data, offset, start, length, precision, signed);
        return d == null ? Double.NaN : d;
    }

    protected static Double parseDouble(byte[] data, int offset, int start, int length, double precision, boolean signed) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, signed);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATAFIELD_ERROR and DATAFIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATAFIELD_UNKNOWN */
        }
        if (length == 64) {
            if (e.v != 0x7FFFFFFFFFFFFFFFL)
                return (double) e.v * precision;
            else
                return null;
        } else {
            if (e.v <= e.m - reserved) {
                return (double) e.v * precision;
            } else {
                return null;
            }
        }
    }

    protected static String getASCII(byte[] b) {
        // remove padding
        int l = b.length;
        char last = (char) b[l - 1];
        while (last == 0xff || last == ' ' || last == 0 || last == '@') {
            l--;
            last = (char) b[l - 1];
        }
        char c;
        int k;

        StringBuilder sb = new StringBuilder(l);
        // escape
        for (k = 0; k < l; k++) {
            c = (char) b[k];
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

                case '\377':
                    // 0xff has been seen on recent Simrad VHF systems, and it seems to indicate
                    // end-of-field, with noise following. Assume this does not break other systems.
                    break;

                default:
                    if (c >= ' ' && c <= '~') {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
