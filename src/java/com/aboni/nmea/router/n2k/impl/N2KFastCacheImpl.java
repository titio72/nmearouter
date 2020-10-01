package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.ServerLog;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class N2KFastCacheImpl implements N2KFastCache {

    private static final String ERR_MSG = "Error handling N2K message";

    private static final long REMOVE_TIMEOUT = 2000;

    private static class Payload {
        N2KMessageParser parser;
        long lastTS;
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

    private N2KMessageCallback callback;

    private final TimestampProvider timestampProvider;
    private final N2KMessageFactory messageFactory;

    @Inject
    public N2KFastCacheImpl(TimestampProvider tsp, @NotNull N2KMessageFactory messageFactory) {
        timestampProvider = tsp;
        this.messageFactory = messageFactory;
    }

    @Override
    public void setCallback(N2KMessageCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onMessage(@NotNull N2KMessage msg) {
        N2KFastEnvelope id = new N2KFastEnvelope();
        id.pgn = msg.getHeader().getPgn();
        id.src = msg.getHeader().getSource();
        if (messageFactory.isSupported(id.pgn)) {
            if (messageFactory.isFast(id.pgn)) {
                handleFastMessage(msg, id);
            } else if (callback != null) {
                N2KMessageParser p = ThingsFactory.getInstance(N2KMessageParser.class);
                try {
                    p.addMessage(msg);
                    callback.onMessage(p.getMessage());
                } catch (PGNDataParseException e) {
                    ServerLog.getLogger().error("N2K message parsing error", e);
                } catch (Exception e) {
                    ServerLog.getLogger().errorForceStacktrace(ERR_MSG, e);
                }
            }
        }
    }

    private void handleFastMessage(N2KMessage msg, N2KFastEnvelope id) {
        int seqId = msg.getData()[0] & 0xFF;
        N2KMessageParser p = getN2KMessageParser(id);
        if (!isEmpty(p) && seqId != (p.getFastSequenceNo() + 1) && isPotentialFirstMessage(msg)) {
            p = handlePrematureEndOfMessage(id, p);
        } else if (isEmpty(p) && !isPotentialFirstMessage(msg)) {
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
        } catch (PGNDataParseException e) {
            ServerLog.getLogger().error("N2K fast message parsing error", e);
            remove = true;
        } catch (Exception e) {
            ServerLog.getLogger().errorForceStacktrace(ERR_MSG, e);
            remove = true;
        }
        if (remove) {
            synchronized (cache) {
                cache.remove(id);
            }
        }
    }

    private N2KMessageParser handlePrematureEndOfMessage(N2KFastEnvelope id, N2KMessageParser p) {
        // "start of sequence" has arrived but there's already a running sequence
        if (callback != null) {
            try {
                callback.onMessage(p.getMessage());
            } catch (PGNDataParseException e) {
                ServerLog.getLogger().error(ERR_MSG, e);
            }
        }
        synchronized (cache) {
            cache.remove(id);
        }
        p = getN2KMessageParser(id);
        return p;
    }

    private boolean isEmpty(N2KMessageParser p) {
        return p.getLength() == 0;
    }

    private boolean isPotentialFirstMessage(N2KMessage msg) {
        int seqId = msg.getData()[0] & 0xFF;
        return (seqId & 0x0F) == 0;
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
            entry.lastTS = (timestampProvider != null) ? timestampProvider.getNow() : System.currentTimeMillis();
        }
        return p;
    }

    @Override
    public void cleanUp() {
        long now = (timestampProvider != null) ? timestampProvider.getNow() : System.currentTimeMillis();
        synchronized (cache) {
            cache.entrySet().removeIf(e -> Utils.isOlderThan(e.getValue().lastTS, now, REMOVE_TIMEOUT));
        }
    }
}
