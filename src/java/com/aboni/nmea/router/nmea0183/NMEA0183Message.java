/*
 * Copyright (c) 2020,  Andrea Boni
 * This file is part of NMEARouter.
 * NMEARouter is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * NMEARouter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with NMEARouter.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.aboni.nmea.router.nmea0183;

import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.impl.*;
import net.sf.marineapi.nmea.sentence.*;

import javax.validation.constraints.NotNull;

public class NMEA0183Message implements Message {

    public static NMEA0183Message get(@NotNull Sentence sentence) {
        switch (sentence.getSentenceId()) {
            case "MMB":
                return new NMEA0183MMBMessage((MMBSentence) sentence);
            case "MTA":
                return new NMEA0183MTAMessage((MTASentence) sentence);
            case "MWV":
                return new NMEA0183MWVMessage((MWVSentence) sentence);
            case "HDM":
                return new NMEA0183HDMMessage((HDMSentence) sentence);
            case "HDG":
                return new NMEA0183HDGMessage((HDGSentence) sentence);
            case "HDT":
                return new NMEA0183HDTMessage((HDTSentence) sentence);
            case "VHW":
                return new NMEA0183VHWMessage((VHWSentence) sentence);
            case "RMC":
                return new NMEA0183RMCMessage((RMCSentence) sentence);
            case "DBT":
                return new NMEA0183DBTMessage((DBTSentence) sentence);
            case "DPT":
                return new NMEA0183DPTMessage((DPTSentence) sentence);
            case "MHU":
                return new NMEA0183MHUMessage((MHUSentence) sentence);
            case "MTW":
                return new NMEA0183MTWMessage((MTWSentence) sentence);
            default:
                return new NMEA0183Message(sentence);
        }
    }

    private final Sentence sentence;

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

    @Override
    public String getMessageType() {
        return sentence.getSentenceId();
    }

    @Override
    public String getMessageOrigin() {
        return sentence.getTalkerId().name();
    }

    @Override
    public String getMessageContentType() {
        return "";
    }
}
