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

import com.aboni.log.Log;
import com.aboni.log.LogStringBuilder;
import com.aboni.log.SafeLog;
import com.aboni.nmea.message.Message;
import com.aboni.nmea.router.*;
import com.aboni.nmea.router.agent.NMEAAgent;
import com.aboni.nmea.router.agent.NMEAAgentStatusListener;
import com.aboni.nmea.router.agent.NMEASource;
import com.aboni.nmea.router.agent.NMEATarget;
import com.aboni.nmea.router.conf.QOS;
import com.aboni.nmea.router.filters.DummyFilter;
import com.aboni.nmea.router.filters.NMEAFilter;
import com.aboni.nmea.router.processors.NMEAPostProcess;
import com.aboni.nmea.router.processors.NMEAProcessorSet;
import com.aboni.nmea.router.processors.NMEARouterProcessorException;
import com.aboni.utils.TimestampProvider;
import net.sf.marineapi.nmea.sentence.Sentence;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class NMEAAgentImpl implements NMEAAgent {

    public static final String AGENT_LIFECYCLE_CATEGORY = "Agent lifecycle";
    public static final String AGENT_MESSAGE_CATEGORY = "Agent message";

    private static class InternalSource implements NMEASource {

        private NMEAFilter filterSet;
        private NMEASentenceListener listener;

        @Override
        public NMEAFilter getFilter() {
            return filterSet;
        }

        @Override
        public void setFilter(NMEAFilter s) {
            filterSet = s;
        }

        @Override
        public void setSentenceListener(NMEASentenceListener listener) {
            this.listener = listener;
        }
    }

    private class InternalTarget implements NMEATarget {

        private NMEAFilter filterSet;

        @Override
        public NMEAFilter getFilter() {
            return filterSet;
        }

        @Override
        public void setFilter(NMEAFilter s) {
            filterSet = s;
        }

        @Override
        public void pushMessage(RouterMessage mm) {
            try {
                if (mm != null && isStarted()) {
                    getListenerWrapper().dispatch(mm);
                }
            } catch (Exception t) {
                log.warning(getLogBuilder().wC(AGENT_MESSAGE_CATEGORY).wO("receive from router").toString(), t);
            }
        }

        private ListenerWrapper getListenerWrapper() {
            if (listenerWrapper == null) {
                listenerWrapper = new ListenerWrapper(NMEAAgentImpl.this, log);
            }
            return listenerWrapper;
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

    private NMEAAgentStatusListener statusListener;
    private final InternalTarget targetIf;
    private final InternalSource sourceIf;
    private final NMEAProcessorSet processorSet;
    private final TimestampProvider timestampProvider;
    private final AgentAttributes attributes;
    private final RouterMessageFactory messageFactory;
    private ListenerWrapper listenerWrapper;
    private final Log log;

    @Inject
    public NMEAAgentImpl(Log log, TimestampProvider timestampProvider, RouterMessageFactory messageFactory, boolean source, boolean target) {
        if (timestampProvider==null) throw new IllegalArgumentException("Timestamp provider cannot be null");
        if (messageFactory == null) throw new IllegalArgumentException("MessageFactory provider cannot be null");
        this.timestampProvider = timestampProvider;
        this.messageFactory = messageFactory;
        this.log = SafeLog.getSafeLog(log);
        this.targetIf = new InternalTarget();
        this.sourceIf = new InternalSource();
        this.attributes = new AgentAttributes();
        this.processorSet = new NMEAProcessorSet();
        this.attributes.canStartStop = true;
        setSourceTarget(source, target);
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
            for (NMEAPostProcess p : ProcessorsBuilder.load(qos, log, timestampProvider)) {
                addProcessor(p);
            }
        }
    }

    protected final void setSourceTarget(boolean isSource, boolean isTarget) {
        attributes.target = isTarget;
        attributes.source = isSource;
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
                log.info(getLogBuilder().wC(AGENT_LIFECYCLE_CATEGORY).wO("activate").toString());
                if (onActivate()) {
                    attributes.active.set(true);
                    notifyStatus();
                } else {
                    log.error(getLogBuilder().wC(AGENT_LIFECYCLE_CATEGORY).wO("activate").toString());
                }
            }
        }
    }

    private void notifyStatus() {
        if (statusListener != null) statusListener.onStatusChange(this);
    }

    @Override
    public final void stop() {
        synchronized (this) {
            if (isStarted()) {
                log.info(getLogBuilder().wC(AGENT_LIFECYCLE_CATEGORY).wO("deactivate").toString());
                onDeactivate();
                attributes.active.set(false);
                notifyStatus();
            }
        }
    }

    @Override
    public final void setStatusListener(NMEAAgentStatusListener listener) {
        statusListener = listener;
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

    private boolean checkSourceFilter(Message m) {
        NMEASource s = getSource();
        if (s!=null && s.getFilter()!=null)
            return s.getFilter().match(messageFactory.createMessage(m, getName(), timestampProvider.getNow()));
        return true;
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param sentence The sentence to be notified to agents
     */
    protected final void postMessage(Sentence sentence) {
        if (sentence != null && sourceIf.listener != null && isStarted()) {
            RouterMessage theMsg = messageFactory.createMessage(sentence, getName(), timestampProvider.getNow());
            if (checkSourceFilter(theMsg.getPayload())) {
                List<Message> toSend = null;
                try {
                    toSend = processorSet.getSentences(theMsg.getPayload(), getName());
                } catch (NMEARouterProcessorException e) {
                    log.error(() -> getLogBuilder().wO("dispatch message").wV("sentence", sentence).toString(), e);
                }
                if (toSend != null)
                    for (Message mToSend : toSend)
                        sourceIf.listener.onSentence(messageFactory.createMessage(mToSend, getName(), timestampProvider.getNow()));
            }
        }
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param m The message to be notified to agents
     */
    protected final void postMessage(JSONObject m) {
        if (m != null && sourceIf.listener != null && isStarted()) {
            sourceIf.listener.onSentence(messageFactory.createMessage(m, getName(), timestampProvider.getNow()));
        }
    }

    /**
     * Used by "sources" to push sentences into the stream
     *
     * @param m The message to be notified to agents
     */
    protected final void postMessage(Message m) {
        if (m != null && sourceIf.listener != null && isStarted() && checkSourceFilter(m)) {
            List<Message> toSend = null;
            try {
                toSend = processorSet.getSentences(m, getName());
            } catch (NMEARouterProcessorException e) {
                log.error(() -> getLogBuilder().wO("dispatch message").wV("sentence", m).toString(), e);
            }
            if (toSend != null)
                for (Message s : toSend)
                    sourceIf.listener.onSentence(messageFactory.createMessage(s, getName(), timestampProvider.getNow()));
        }
    }

    /**
     * Sources can use post-process delegates to add additional elaboration to the sentences they push into the stream.
     *
     * @param f The post processor to be added (sequence is important)
     */
    protected final void addProcessor(NMEAPostProcess f) {
        processorSet.addProcessor(f);
    }

    protected LogStringBuilder getLogBuilder() {
        return LogStringBuilder.start("Agent").wV("Agent", getName()).wV("Type", getType());
    }

    protected Log getLog() {
        return log;
    }

    protected TimestampProvider getTimestampProvider() {
        return timestampProvider;
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

    @Override
    public JSONObject toJSON() {
        JSONObject agJSON = new JSONObject();
        agJSON.put("agent", getName());
        agJSON.put("description", getDescription());
        agJSON.put("type", getType());
        agJSON.put("started", isStarted());
        agJSON.put("source", (getSource() != null));
        agJSON.put("target", (getTarget() != null));
        agJSON.put("startStop", isUserCanStartAndStop());
        agJSON.put("builtin", isBuiltIn());
        NMEASource src = getSource();
        if (src!=null) {
            agJSON.put("hasSourceFilter", hasFilter(src));
            if (hasFilter(src)) agJSON.put("sourceFilter", src.getFilter().toJSON());
        } else {
            agJSON.put("hasSourceFilter", false);
        }
        NMEATarget trg = getTarget();
        if (trg!=null) {
            agJSON.put("hasTargetFilter", hasFilter(trg));
            if (trg.getFilter()!=null) agJSON.put("targetFilter", trg.getFilter().toJSON());
        } else {
            agJSON.put("hasTargetFilter", false);
        }
        return agJSON;
    }

    private static boolean hasFilter(NMEAFilterable f) {
        return !(f.getFilter() == null || f.getFilter() instanceof DummyFilter);
    }
}
