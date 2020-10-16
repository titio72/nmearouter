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
import net.sf.marineapi.nmea.sentence.Sentence;

import javax.validation.constraints.NotNull;

public class NMEA0183Message implements Message {

    public static NMEA0183Message get(@NotNull Sentence sentence) {
        return new NMEA0183Message(sentence);
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
}
