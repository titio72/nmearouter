package com.aboni.nmea.router.n2k.can;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefinitions;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class N2KFastCache {

    private static final long REMOVE_TIMEOUT = 2000;

    private static class Payload {
        N2KMessageParser parser;
        long lastTS;
        int lastSeq;
    }

    private static class N2KFastEnvelope {
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

    private final Map<N2KFastEnvelope, Payload> cache = new HashMap<>();

    private final N2KMessageCallback callback;

    public N2KFastCache(N2KMessageCallback callback) {
        this.callback = callback;
    }

    public void onMessage(@NotNull N2KMessage msg) {
        N2KFastEnvelope id = new N2KFastEnvelope();
        id.pgn = msg.getHeader().getPgn();
        id.src = msg.getHeader().getSource();
        N2KMessageDefinitions.N2KDef d = N2KMessageDefinitions.getDefinition(id.pgn);
        if (d != null) {
            if (d.isFast()) {
                handleFastMessage(msg, id);
            } else if (callback != null) {
                N2KMessageParser p = ThingsFactory.getInstance(N2KMessageParser.class);
                try {
                    p.addMessage(msg);
                    callback.onMessage(p.getMessage());
                } catch (PGNDataParseException e) {
                    ServerLog.getLogger().error("Error handling N2K message", e);
                }
            }
        }
    }

    private void handleFastMessage(N2KMessage msg, N2KFastEnvelope id) {
        int seqId = msg.getData()[0] & 0xFF;
        N2KMessageParser p = getN2KMessageParser(id);
        if (p.getLength() != 0 && (seqId & 0x0F) == 0) {
            // "start of sequence" has arrived but there's already a running sequence
            if (callback != null) {
                try {
                    callback.onMessage(p.getMessage());
                } catch (PGNDataParseException e) {
                    ServerLog.getLogger().error("Error handling N2K message", e);
                }
            }
            synchronized (cache) {
                cache.remove(id);
            }
            p = getN2KMessageParser(id);
        } else if (p.getLength() == 0 && (seqId & 0x0F) != 0) {
            // not a "start of sequence" but no parser is available - skip it because the previous messages were lost
            return;
        }

        boolean remove = false;
        try {
            p.addMessage(msg);
            if (!p.needMore()) {
                remove = true;
                if (callback != null) callback.onMessage(p.getMessage());
            }
        } catch (PGNFastException e) {
            ServerLog.getLogger().debug("Out of sequence message {" + msg + "} err {" + e.getMessage() + "}");
            remove = true;
        } catch (Exception e) {
            ServerLog.getLogger().error("Error handling N2K message", e);
            remove = true;
        }
        if (remove) {
            synchronized (cache) {
                cache.remove(id);
            }
        }
    }

    private N2KMessageParser getN2KMessageParser(N2KFastEnvelope id) {
        N2KMessageParser p;
        synchronized (cache) {
            Payload entry = cache.getOrDefault(id, null);
            if (entry == null) {
                entry = new Payload();
                p = ThingsFactory.getInstance(N2KMessageParser.class);
                entry.parser = p;
                cache.put(id, entry);
            } else {
                p = entry.parser;
            }
            entry.lastTS = System.currentTimeMillis();
        }
        return p;
    }

    public void onTimer() {
        long now = System.currentTimeMillis();
        synchronized (cache) {
            cache.entrySet().removeIf(e -> Utils.isOlderThan(e.getValue().lastTS, now, REMOVE_TIMEOUT));
        }
    }
}
