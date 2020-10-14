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

package com.aboni.nmea.router.processors;

import com.aboni.geo.NMEAMagnetic2TrueConverter;
import com.aboni.misc.Utils;
import com.aboni.nmea.router.message.Message;
import com.aboni.nmea.router.nmea0183.NMEA0183Message;
import com.aboni.utils.Pair;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.RMCSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.VTGSentence;
import net.sf.marineapi.nmea.util.FaaMode;

import java.time.OffsetDateTime;

/**
 * Used to produce a VTG sentence from a RMC to match requirement of NKE
 *
 * @author aboni
 */
public class NMEARMC2VTGProcessor implements NMEAPostProcess {

    private final NMEAMagnetic2TrueConverter m;

    public NMEARMC2VTGProcessor() {
        this(OffsetDateTime.now().getYear());
    }

    public NMEARMC2VTGProcessor(double year) {
        m = new NMEAMagnetic2TrueConverter(year);
    }

    private static RMCSentence getRMC(Message m)  {
        if (m instanceof NMEA0183Message &&
                ((NMEA0183Message) m).getSentence() instanceof RMCSentence) {
            return (RMCSentence)((NMEA0183Message)m).getSentence();
        } else {
            return null;
        }
    }

    @Override
    public Pair<Boolean, Message[]> process(Message message, String src) throws NMEARouterProcessorException {
        try {
            RMCSentence rmc = getRMC(message);
            if (rmc!=null) {
                VTGSentence vtg = (VTGSentence) SentenceFactory.getInstance().createParser(rmc.getTalkerId(), SentenceId.VTG);
                vtg.setMode(FaaMode.AUTOMATIC);
                vtg.setSpeedKnots(rmc.getSpeed());
                vtg.setSpeedKmh(rmc.getSpeed() * 1.852);
                setHeading(rmc, vtg);
                return new Pair<>(Boolean.TRUE, new Message[]{new NMEA0183Message(vtg)});
            }
        } catch (Exception e) {
            throw new NMEARouterProcessorException("Error converting RMC sentence \"" + message + "\"", e);
        }
        return new Pair<>(Boolean.TRUE, null);
    }

    private void setHeading(RMCSentence rmc, VTGSentence vtg) {
        try {
            vtg.setTrueCourse(rmc.getCourse());
            m.setPosition(rmc.getPosition());
            double mag = m.getMagnetic(rmc.getCourse(), rmc.getPosition());
            mag = Utils.normalizeDegrees0To360(mag);
            vtg.setMagneticCourse(mag);
        } catch (DataNotAvailableException e) {
            // stationary, no course (i.e. v=0.0)
            vtg.setMagneticCourse(0.0);
            vtg.setTrueCourse(0.0);
        }
    }

    @Override
    public void onTimer() {
        // nothing to do
    }

}
