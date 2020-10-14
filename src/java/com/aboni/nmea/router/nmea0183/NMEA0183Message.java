package com.aboni.nmea.router.nmea0183;

import com.aboni.nmea.router.message.Message;
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEA0183Message implements Message {

    public static NMEA0183Message get(@NotNull Sentence sentence) {
        return new NMEA0183Message(sentence);
    }

    private Sentence sentence;

    public NMEA0183Message(@NotNull Sentence sentence) {
        this.sentence = sentence;
    }

    public Sentence getSentence() {
        return sentence;
    }

    @Override
    public String toString() {
        return String.format("Message {%s}", sentence);
    }
}
