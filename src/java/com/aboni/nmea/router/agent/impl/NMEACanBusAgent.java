package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.agent.QOS;
import com.aboni.nmea.router.n2k.N2KMessage;
import com.aboni.nmea.router.n2k.N2KMessage2NMEA0183;
import com.aboni.nmea.router.n2k.can.N2KCanReader;
import com.aboni.nmea.router.n2k.can.N2KFastCache;
import com.aboni.nmea.router.n2k.impl.N2KMessageDefinitions;
import com.aboni.utils.SerialReader;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class NMEACanBusAgent extends NMEAAgentImpl {

    private String portName;
    private int speed;

    private SerialReader serialReader;
    private N2KCanReader canReader;
    private N2KFastCache fastCache;
    private N2KMessage2NMEA0183 converter;

    @Inject
    public NMEACanBusAgent(@NotNull NMEACache cache, N2KMessage2NMEA0183 converter) {
        super(cache);
        setSourceTarget(true, false);
        fastCache = new N2KFastCache(this::onRecv);
        canReader = new N2KCanReader(fastCache::onMessage);
        serialReader = new SerialReader();
        this.converter = converter;
    }

    private void onRecv(@NotNull N2KMessage msg) {
        if (N2KMessageDefinitions.isSupported(msg.getHeader().getPgn())) {
            notify(msg);
            if (converter!=null) {
                Sentence[] s = converter.getSentence(msg);
                if (s!=null) {
                    for (Sentence ss: s) notify(ss);
                }
            }
        }
    }

    public void setup(String name, QOS qos, String port, int speed) {
        super.setup(name, qos);
        portName = port;
        this.speed = speed;
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
