package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageHeader;

import java.time.Instant;
import java.util.Map;

public abstract class N2KMessageImpl implements N2KMessage {

    protected final N2KMessageHeader header;
    protected final byte[] data;

    protected boolean isValidByte(int b) {
        return b != 0xFF;
    }

    protected boolean isValidDouble(double d) {
        return !Double.isNaN(d);
    }

    private static class DefaultHeader implements N2KMessageHeader {
        final int pgn;
        final Instant now;

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

    protected static String parseEnum(byte[] data, int offset, int start, int length, Map<Integer, String> map) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, false);
        int reserved = 0;
        if (e.m >= 15) {
            reserved = 2; /* DATA FIELD_ERROR and DATA FIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATA FIELD_UNKNOWN */
        }

        String ret = null;
        if (e.v <= (e.m - reserved)) {
            ret = map.getOrDefault((int) e.v, String.format("%d", e.v));
        }
        return ret;
    }

    protected static long parseIntegerSafe(byte[] data, int offset, int start, int length, long def) {
        if (offset + length > (data.length * 8)) return def;

        BitUtils.Res e = BitUtils.extractBits(data, start, offset, length, false);
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

    protected static Long parseInteger(byte[] data, int offset, int length) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, 0, offset, length, false);
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

    protected static double parseDoubleSafe(byte[] data, int offset, int length, double precision, boolean signed) {
        Double d = parseDouble(data, offset, length, precision, signed);
        return d == null ? Double.NaN : d;
    }

    protected static Double parseDouble(byte[] data, int offset, int length, double precision, boolean signed) {
        if (offset + length > (data.length * 8)) return null;

        BitUtils.Res e = BitUtils.extractBits(data, 0, offset, length, signed);
        int reserved = 0;
        if (e.v >= 15) {
            reserved = 2; /* DATA FIELD_ERROR and DATA FIELD_UNKNOWN */
        } else if (e.m > 1) {
            reserved = 1; /* DATA FIELD_UNKNOWN */
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

    protected static String getText(byte[] data, int byteStart, int byteLength) {

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

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        if (getHeader()!=null) {
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b: getData()) {
                stringBuilder.append( String.format(" %x", (b & 0xFF)) );
            }
            return String.format("PGN {%d} Source {%d} Data {%s}", getHeader().getPgn(), getHeader().getSource(), stringBuilder.toString());
        } else
            return super.toString();
    }
}
