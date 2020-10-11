package com.aboni.nmea.router.nmea0183;

import com.aboni.nmea.router.message.Message;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEA0183Message implements Message {

    private Sentence sentence;

    protected NMEA0183Message(@NotNull Sentence sentence) {
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return sentence;
    }

}
