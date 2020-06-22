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

package com.aboni.nmea.router;

import com.aboni.utils.DataEvent;
import net.sf.marineapi.nmea.sentence.HeadingSentence;
import net.sf.marineapi.nmea.sentence.PositionSentence;
import net.sf.marineapi.nmea.sentence.Sentence;

public interface NMEACache {

    DataEvent<HeadingSentence> getLastHeading();

    DataEvent<PositionSentence> getLastPosition();

    boolean isHeadingOlderThan(long time, long threshold);

    void onSentence(Sentence s, String src);

    <T> void setStatus(String statusKey, T status);

    <T> T getStatus(String statusKey, T defaultValue);

    long getNow();

}