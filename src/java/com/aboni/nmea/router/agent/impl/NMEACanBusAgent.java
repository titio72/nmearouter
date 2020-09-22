package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.PGNSourceFilter;
import com.aboni.nmea.router.n2k.can.N2KCanReader;
import com.aboni.nmea.router.n2k.can.N2KFastCache;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefinitions;
import com.aboni.utils.SerialReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEACanBusAgent extends NMEAAgentImpl {

    private final SerialReader serialReader;
    private final N2KCanReader canReader;
    private final N2KMessage2NMEA0183 converter;
    private final PGNSourceFilter srcFilter;

    @Inject
    public NMEACanBusAgent(@NotNull NMEACache cache, N2KMessage2NMEA0183 converter) {
        super(cache);
        setSourceTarget(true, false);
        N2KFastCache fastCache = new N2KFastCache(this::onReceive);
        canReader = new N2KCanReader(fastCache::onMessage);
        canReader.setErrCallback(this::onError);
        serialReader = new SerialReader();
        srcFilter = new PGNSourceFilter(getLogger());
        this.converter = converter;
    }

    private void onError(byte[] buffer) {
        StringBuilder sb = new StringBuilder("NMEACanBusAgent Error decoding buffer {");
        for (byte b : buffer) {
            sb.append(String.format(" %02x", b));
        }
        sb.append("}");
        getLogger().error(sb.toString());
    }

    private void onReceive(@NotNull N2KMessage msg) {
        if (srcFilter.accept(msg.getHeader().getSource(), msg.getHeader().getPgn())
                && N2KMessageDefinitions.isSupported(msg.getHeader().getPgn())) {
            notify(msg);
            if (converter != null) {
                Sentence[] s = converter.getSentence(msg);
                if (s != null) {
                    for (Sentence ss : s) notify(ss);
                }
            }
        }
    }

    public void setup(String name, QOS qos, String port, int speed) {
        super.setup(name, qos);
        serialReader.setup(port, speed, canReader::onRead);
    }

    @Override
    public String getType() {
        return "CAN Bus N2K Receiver";
    }

    @Override
    public String getDescription() {
        return getType();
    }

    @Override
    public String toString() {
        return getDescription();
    }

    @Override
    protected boolean onActivate() {
        if (super.onActivate()) {
            serialReader.activate();
            return true;
        }
        return false;
    }

    @Override
    protected void onDeactivate() {
        super.onDeactivate();
        serialReader.deactivate();
    }
}
