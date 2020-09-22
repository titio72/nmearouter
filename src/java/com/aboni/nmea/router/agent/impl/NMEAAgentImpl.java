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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.NMEASentenceListener;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.agent.*;
import com.aboni.nmea.router.filters.NMEAFilterSet;
import com.aboni.nmea.router.impl.ListenerWrapper;
import com.aboni.nmea.router.impl.RouterMessageImpl;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.processors.NMEAPostProcess;
import com.aboni.nmea.router.processors.NMEAProcessorSet;
import com.aboni.utils.Log;
import com.aboni.utils.ServerLog;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEAAgentImpl implements NMEAAgent {

    private static class InternalSource implements NMEASource {

        NMEAFilterSet filterSet;
        NMEASentenceListener listener;

        @Override
        public NMEAFilterSet getFilter() {
            return filterSet;
        }

        @Override
        public void setFilter(NMEAFilterSet s) {
            filterSet = s;
        }

        @Override
        public void setSentenceListener(NMEASentenceListener listener) {
            this.listener = listener;
        }
    }

    private class InternalTarget implements NMEATarget {

        NMEAFilterSet filterSet;

        @Override
        public NMEAFilterSet getFilter() {
            return filterSet;
        }

        @Override
        public void setFilter(NMEAFilterSet s) {
            filterSet = s;
        }

        @Override
        public void pushMessage(RouterMessage mm) {
            try {
                if (isStarted()) {
                    lazyLoadListenerWrapper();
                    if (handleNMEA0183(mm)) return;
                    if (handleNMEA2000(mm)) return;
                    if (handleJSON(mm)) return;
                    getLogger().error(String.format("Unknown message type {%s}", mm.getPayload()));
                }
            } catch (Exception t) {
                getLogger().warning("Error delivering message to agent {" + mm.getPayload() + "} error {" + t.getMessage() + "}");
            }
        }

        private boolean handleJSON(RouterMessage mm) {
            if (mm.getPayload() instanceof JSONObject) {
                if (listenerWrapper.isJSON()) listenerWrapper.onSentence((JSONObject) mm.getPayload(), mm.getSource());
                return true;
            }
            return false;
        }

        private boolean handleNMEA2000(RouterMessage mm) {
            if (mm.getPayload() instanceof N2KMessage) {
                if (listenerWrapper.isN2K() && (getFilter()==null || getFilter().match(mm))) {
                    listenerWrapper.onSentence((N2KMessage) mm.getPayload(), mm.getSource());
                }
                return true;
            }
            return false;
        }

        private boolean handleNMEA0183(RouterMessage mm) {
            if (mm.getPayload() instanceof Sentence) {
                Sentence s = (Sentence) mm.getPayload();
                if (listenerWrapper.isNMEA() && (getFilter() == null || getFilter().match(mm))) {
                    listenerWrapper.onSentence(s, mm.getSource());
                }
                return true;
            }
            return false;
        }

        private void lazyLoadListenerWrapper() {
            if (listenerWrapper == null) {
                listenerWrapper = new ListenerWrapper(NMEAAgentImpl.this);
            }
        }
    }

    private static class AgentAttributes {
        String name;
        final AtomicBoolean active = new AtomicBoolean();
        boolean builtin;
        boolean target;
        boolean source;
        boolean canStartStop;
    }

    protected class PrivateLog implements Log {

        private final Log log;

        private PrivateLog() {
            log = ServerLog.getLogger();
        }

        private String getMsg(NMEAAgentImpl a, String msg) {
            return String.format("Agent {%s} Name {%s} %s", a.toString(), a.getName(), msg);
        }

        @Override
        public void error(String msg) {
            log.error(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void error(String msg, Throwable t) {
            log.error(getMsg(NMEAAgentImpl.this, msg), t);
        }

        @Override
        public void errorForceStacktrace(String msg, Throwable t) {
            log.errorForceStacktrace(getMsg(NMEAAgentImpl.this, msg), t);
        }

        @Override
        public void warning(String msg) {
            log.warning(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void info(String msg) {
            log.info(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void infoFill(String msg) {
            log.infoFill(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void debug(String msg) {
            log.debug(getMsg(NMEAAgentImpl.this, msg));
        }

        @Override
        public void console(String msg) {
            throw new UnsupportedOperationException();
        }
    }

    private NMEAAgentStatusListener sl;
    private final InternalTarget targetIf;
    private final InternalSource sourceIf;
    private final NMEAProcessorSet processorSet;
    private final NMEACache cache;
    private final AgentAttributes attributes;
    private final Log internalLog;
    private ListenerWrapper listenerWrapper;

    @Inject
    public NMEAAgentImpl(@NotNull NMEACache cache) {
        this.cache = cache;
        targetIf = new InternalTarget();
        sourceIf = new InternalSource();
        attributes = new AgentAttributes();
        processorSet = new NMEAProcessorSet();
        internalLog = new PrivateLog();
        attributes.canStartStop = true;
    }

    @Override
    public void setup(String name, QOS qos) {
        attributes.name = name;
        handleQos(qos);
        onSetup(name, qos);
    }

    protected void onSetup(String name, QOS qos) {
        // override if necessary
    }

    private void handleQos(QOS qos) {
        if (qos != null) {
            attributes.builtin = qos.get(QOSKeys.BUILT_IN);
            attributes.canStartStop = !qos.get(QOSKeys.CANNOT_START_STOP);
            for (NMEAPostProcess p : ProcessorsBuilder.load(qos, getLogger())) {
                addProcessor(p);
            }
        }
    }

    protected void setSourceTarget(boolean isSource, boolean isTarget) {
        attributes.target = isTarget;
        attributes.source = isSource;
    }

    protected Log getLogger() {
        return internalLog;
    }

    @Override
    public final boolean isBuiltIn() {
        return attributes.builtin;
    }

    @Override
    public final String getName() {
        return attributes.name;
    }

    @Override
    public final boolean isStarted() {
        return attributes.active.get();
    }

    @Override
    public final void start() {
        synchronized (this) {
            if (!isStarted()) {
                getLogger().info("Activating agent {" + getName() + "}");
                if (onActivate()) {
                    attributes.active.set(true);
                    notifyStatus();
                } else {
                    getLogger().error("Cannot activate agent {" + getName() + "}");
                }
            }
        }
    }

    private void notifyStatus() {
        if (sl!=null) sl.onStatusChange(this);
    }

    @Override
    public final void stop() {
        synchronized (this) {
            if (isStarted()) {
                getLogger().info("Deactivating {" + getName() + "}");
                onDeactivate();
                attributes.active.set(false);
                notifyStatus();
            }
        }
    }

    @Override
    public final void setStatusListener(NMEAAgentStatusListener listener) {
        sl = listener;
    }

    @Override
    public String toString() {
        return "{" + getType() + "}";
    }

    protected boolean onActivate() {
        return true;
    }

    protected void onDeactivate() {
        // override if necessary
    }

    private boolean checkSourceFilter(Sentence sentence) {
        NMEASource s = getSource();
        if (s!=null && s.getFilter()!=null)
            return s.getFilter().match(RouterMessageImpl.createMessage(sentence, getName(), cache.getNow()));
        return true;
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param sentence The sentence to be notified to agents
     */
    protected final void notify(Sentence sentence) {
        if (isStarted() && checkSourceFilter(sentence) && sourceIf.listener != null) {
            List<Sentence> toSend = processorSet.getSentences(sentence, getName());
            for (Sentence s : toSend)
                sourceIf.listener.onSentence(RouterMessageImpl.createMessage(s, getName(), cache.getNow()));
        }
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param m The message to be notified to agents
     */
    protected final void notify(JSONObject m) {
        if (isStarted()) {
            sourceIf.listener.onSentence(RouterMessageImpl.createMessage(m, getName(), cache.getNow()));
        }
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param m The message to be notified to agents
     */
    protected final void notify(N2KMessage m) {
        if (isStarted()) {
            getLogger().debug("Notify Sentence {" + m + "}");
            sourceIf.listener.onSentence(RouterMessageImpl.createMessage(m, getName(), cache.getNow()));
        }
    }

    /**
     * Sources can use post-process delegates to add additional elaboration to the sentences they pushes into the stream.
     *
     * @param f The post processor to be added (sequence is important)
     */
    protected final void addProcessor(NMEAPostProcess f) {
        processorSet.addProcessor(f);
    }

    @Override
    public final NMEASource getSource() {
        return (attributes.source ? sourceIf : null);
    }

    @Override
    public final NMEATarget getTarget() {
        return (attributes.target ? targetIf : null);
    }

    public final boolean isSource() {
        return getSource() != null;
    }

    public final boolean isTarget() {
        return getTarget() != null;
    }

    @Override
    public final boolean isUserCanStartAndStop() {
        return attributes.canStartStop;
    }

    @Override
    public String getType() {
        return getClass().getSimpleName();
    }

    @Override
    public String getDescription() {
        return "Agent \"" + getType() + "\"";
    }

    @Override
    public void onTimerHR() {
        // override if necessary
    }

    @Override
    public void onTimer() {
        if (isStarted()) {
            processorSet.onTimer();
        }
    }

    protected NMEACache getCache() {
        return cache;
    }
}
