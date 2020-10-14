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

package com.aboni.nmea.router.nmea0183.impl;

import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.nmea.router.nmea0183.NMEA0183MessageFactory;
import net.sf.marineapi.nmea.sentence.*;

import javax.inject.Inject;

public class NMEA0183MessageFactoryImpl implements NMEA0183MessageFactory {

    @Inject
    public NMEA0183MessageFactoryImpl() {
        // do nothing
    }

    @Override
    public Message getMessage(Sentence sentence) {
        if (sentence!=null) {
            switch (sentence.getSentenceId()) {
                case "MHU": return new NMEA0183MHUMessage((MHUSentence)sentence);
                case "MMB": return new NMEA0183MMBMessage((MMBSentence)sentence);
                case "MTW": return new NMEA0183MTWMessage((MTWSentence)sentence);
                case "MTA": return new NMEA0183MTAMessage((MTASentence)sentence);
                case "RMC": return new NMEA0183RMCMessage((RMCSentence)sentence);
                case "MWV": return new NMEA0183MWVMessage((MWVSentence)sentence);
                case "VHW": return new NMEA0183VHWMessage((VHWSentence)sentence);
                case "HDM": return new NMEA0183HDMMessage((HDMSentence)sentence);
                case "HDG": return new NMEA0183HDGMessage((HDGSentence)sentence);
                case "HDT": return new NMEA0183HDTMessage((HDTSentence)sentence);
                case "DBT": return new NMEA0183DBTMessage((DBTSentence)sentence);
                case "DPT": return new NMEA0183DPTMessage((DPTSentence)sentence);
                default: return new NMEA0183Message(sentence);
            }
        }
        return null;
    }
}
