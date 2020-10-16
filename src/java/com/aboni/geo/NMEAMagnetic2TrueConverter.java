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

package com.aboni.geo;

import com.aboni.misc.Utils;
import com.aboni.nmea.router.Constants;
import com.aboni.utils.LogAdmin;
import com.aboni.utils.ThingsFactory;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;

import java.time.OffsetDateTime;

public class NMEAMagnetic2TrueConverter {

    private final TSAGeoMag geo;
    private Position pos;
    private final double year;

    public NMEAMagnetic2TrueConverter() {
        geo = new TSAGeoMag(Constants.WMM, ThingsFactory.getInstance(LogAdmin.class).getBaseLogger());
        pos = new Position(43.0, 10.0);

        OffsetDateTime odt = OffsetDateTime.now();
        this.year = (double) odt.getYear() + ((double) odt.getMonthValue() / 12.0);
    }

    public NMEAMagnetic2TrueConverter(double year) {
        geo = new TSAGeoMag(Constants.WMM, ThingsFactory.getInstance(LogAdmin.class).getBaseLogger());
        pos = new Position(43.0, 10.0);
        this.year = year;
    }

    public double getTrue(double magnetic) {
        double declination = geo.getDeclination(pos.getLatitude(), pos.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(magnetic + declination);
    }

    public double getTrue(double magnetic, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(magnetic + declination);
    }

    public double getMagnetic(double trueCourse, Position p) {
        double declination = geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
        return Utils.normalizeDegrees0To360(trueCourse - declination);
    }

    public double getDeclination(Position p) {
        return geo.getDeclination(p.getLatitude(), p.getLongitude(), year, 0);
    }

    public Position getPosition() {
        return pos;
    }

    public void setPosition(PositionSentence s) {
        setPosition(s.getPosition());
    }

    public void setPosition(Position s) {
        pos = s;
    }

    public HDTSentence getTrueSentence(HDMSentence magSentence) {
        return getTrueSentence(magSentence.getTalkerId(), magSentence.getHeading());
    }

    public HDTSentence getTrueSentence(TalkerId tid, double magBearing) {
        double trueHeading = getTrue(magBearing, getPosition());
        HDTSentence s = (HDTSentence) SentenceFactory.getInstance().createParser(tid, SentenceId.HDT);
        s.setHeading(trueHeading);
        return s;
    }

    public HDGSentence getSentence(TalkerId tid, double bearing, double deviation) {
        HDGSentence s = (HDGSentence)SentenceFactory.getInstance().createParser(tid, SentenceId.HDG);
        s.setHeading(bearing);
        s.setDeviation(deviation);
        s.setVariation(getDeclination(getPosition()));
        return s;
    }

}
