package com.aboni.nmea.router.n2k.impl;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.*;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;
import com.aboni.utils.Log;
import com.aboni.utils.LogStringBuilder;
import com.aboni.utils.ThingsFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class N2KFastCacheImpl implements N2KFastCache {

    private static final long REMOVE_TIMEOUT = 2000;
    public static final String N2K_FAST_CACHE_CATEGORY = "N2KFastCache";
    public static final String MESSAGE_KEY_NAME = "message";
    public static final String FAST_MESSAGE_HANDLING_KEY_NAME = "fast message handling";
    public static final String ERROR_TYPE_KEY_NAME = "error type";
    private final Log log;

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
    public N2KFastCacheImpl(@NotNull Log log, TimestampProvider tsp, @NotNull N2KMessageFactory messageFactory) {
        timestampProvider = tsp;
        this.log = log;
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
                } catch (Exception e) {
                    LogStringBuilder.start(N2K_FAST_CACHE_CATEGORY).wO("message received").wV(MESSAGE_KEY_NAME, msg).error(log, e);
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
            LogStringBuilder.start(N2K_FAST_CACHE_CATEGORY).wO(FAST_MESSAGE_HANDLING_KEY_NAME).wV(MESSAGE_KEY_NAME, msg).wV(ERROR_TYPE_KEY_NAME, "out of sequence message").error(log, e);
            remove = true;
        } catch (PGNDataParseException e) {
            LogStringBuilder.start(N2K_FAST_CACHE_CATEGORY).wO(FAST_MESSAGE_HANDLING_KEY_NAME).wV(MESSAGE_KEY_NAME, msg).wV(ERROR_TYPE_KEY_NAME, "parsing").error(log, e);
            remove = true;
        } catch (Exception e) {
            LogStringBuilder.start(N2K_FAST_CACHE_CATEGORY).wO(FAST_MESSAGE_HANDLING_KEY_NAME).wV(MESSAGE_KEY_NAME, msg).errorForceStacktrace(log, e);
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
                LogStringBuilder.start(N2K_FAST_CACHE_CATEGORY).wO(FAST_MESSAGE_HANDLING_KEY_NAME).wV(ERROR_TYPE_KEY_NAME, "premature end of message").error(log, e);
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
