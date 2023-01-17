package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.N2KMessageParser;
import com.aboni.nmea.router.n2k.N2KMessageParserFactory;
import com.aboni.nmea.router.n2k.messages.N2KMessageFactory;

import javax.inject.Inject;

public class N2KMessageParserFactoryImpl implements N2KMessageParserFactory {

    private final N2KMessageFactory messageFactory;

    @Inject
    public N2KMessageParserFactoryImpl(N2KMessageFactory messageFactory) {
        if (messageFactory==null) throw new IllegalArgumentException("Message factory is null");
        this.messageFactory = messageFactory;
    }

    @Override
    public N2KMessageParser getNewParser() {
        return new N2KMessageParserImpl(messageFactory);
    }
}
