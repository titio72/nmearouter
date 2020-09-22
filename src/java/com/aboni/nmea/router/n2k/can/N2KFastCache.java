package com.aboni.nmea.router.n2k.can;

import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.PGNDataParseException;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class N2KFastCache {

    private class N2KFastEnvelope {
        int pgn;
        int src;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            N2KFastEnvelope that = (N2KFastEnvelope) o;
            return pgn == that.pgn &&
                    src == that.src;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pgn, src);
        }
    }

    private final Map<N2KFastEnvelope, N2KMessageParser> cache = new HashMap<>();

    private final N2KMessageCallback callback;

    public N2KFastCache(N2KMessageCallback callback) {
        this.callback = callback;
    }

    public void onMessage(@NotNull N2KMessage msg) {
        N2KFastEnvelope id = new N2KFastEnvelope();
        id.pgn = msg.getHeader().getPgn();
        id.src = msg.getHeader().getSource();

        N2KMessageParser p = cache.getOrDefault(id, null);
        if (p == null) {
            p = ThingsFactory.getInstance(N2KMessageParser.class);
            cache.put(id, p);
        }

        try {
            p.addMessage(msg);
            if (!p.needMore()) {
                cache.remove(id);
                if (callback != null) callback.onRead(p.getMessage());
            }
        } catch (PGNDataParseException e) {
            ServerLog.getLogger().error("Error handling N2K message");
            e.printStackTrace();
            cache.remove(id);
        }
    }
}
