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

package com.aboni.nmea.router.n2k.messages.impl;

import com.aboni.nmea.router.AISStaticData;

public class N2KAISStaticDataBImpl implements AISStaticData {

    public N2KAISStaticDataBPartAImpl getPartA() {
        return partA;
    }

    public void setPartA(N2KAISStaticDataBPartAImpl partA) {
        this.partA = partA;
    }

    public N2KAISStaticDataBPartBImpl getPartB() {
        return partB;
    }

    public void setPartB(N2KAISStaticDataBPartBImpl partB) {
        this.partB = partB;
    }

    private N2KAISStaticDataBPartAImpl partA;
    private N2KAISStaticDataBPartBImpl partB;

    @Override
    public int getMessageId() {
        if (partA != null) return partA.getMessageId();
        else if (partB != null) return partB.getMessageId();
        else return 0xFF;
    }

    @Override
    public String getMMSI() {
        if (partA != null) return partA.getMMSI();
        else if (partB != null) return partB.getMMSI();
        else return null;
    }

    @Override
    public int getRepeatIndicator() {
        if (partA != null) return partA.getRepeatIndicator();
        else if (partB != null) return partB.getRepeatIndicator();
        else return 0;
    }

    @Override
    public String getName() {
        if (partA != null) return partA.getName();
        else return null;
    }

    @Override
    public String getCallSign() {
        if (partB != null) return partB.getCallSign();
        else return null;
    }

    @Override
    public String getAISClass() {
        return "B";
    }

    @Override
    public double getLength() {
        if (partB != null) return partB.getLength();
        else return 0;
    }

    @Override
    public double getBeam() {
        if (partB != null) return partB.getBeam();
        else return 0;
    }

    @Override
    public String getTypeOfShip() {
        if (partB != null) return partB.getTypeOfShip();
        else return null;
    }

    @Override
    public String getAisTransceiverInfo() {
        if (partA != null) return partA.getAisTransceiverInfo();
        else if (partB != null) return partB.getAisTransceiverInfo();
        else return null;
    }
}
