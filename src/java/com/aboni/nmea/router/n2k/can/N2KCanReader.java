package com.aboni.nmea.router.n2k.can;

public class N2KCanReader {

    private final N2KMessageCallback callback;

    public N2KCanReader(N2KMessageCallback messageCallback) {
        callback = messageCallback;
    }

    public boolean onRead(int[] b, int offset) {
        if (b[offset] == 0xaa && offset > 1 && b[offset - 1] == 0x55) {
            // Note: b[0] is always 0xaa - frames are separated by a (0xaa, 0xaa)
            handleFrame(b, offset);
            return true;
        }
        return false;
    }

    private void handleFrame(int[] b, int offset) {
        int dataSize = (b[1] & 0x0F);
        long id;
        boolean ext = (b[1] & 0x20) != 0;
        if (offset == (3 + dataSize + (ext ? 4 : 2))) {
            if (ext) {
                id = b[2] + (b[3] << 8) + (b[4] << 16) + ((long) b[5] << 24);
            } else {
                id = b[2] + (b[3] << 8);
            }
            //dumpCandumpFormat(offset, b, dataSize, id);
            dumpAnalyzerFormat(offset, b, dataSize, id);

        } else {
            System.out.println("Error");
            // Log error
        }
    }

    private void dumpAnalyzerFormat(int offset, int[] b, int dataSize, long id) {
        if (callback != null) {
            N2KHeader iso = new N2KHeader(id);
            byte[] data = new byte[dataSize];
            for (int i = 0; i < dataSize; i++) data[i] = (byte) (b[offset - 1 - dataSize + i] & 0xFF);
            N2KMessageImpl msg = new N2KMessageImpl(iso, data);
            callback.onRead(msg);
        }
    }

    private void dumpCandumpFormat(int offset, int[] b, int dataSize, long id) {
        StringBuilder res = new StringBuilder(String.format("  %08x  [%d]", id, dataSize));
        for (int i = 0; i < dataSize; i++)
            res.append(String.format(" %02x", b[offset - 1 - dataSize + i]));

        System.out.println(res.toString());
    }
}
