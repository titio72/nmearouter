package com.aboni.nmea.router.n2k.impl;

import com.aboni.nmea.router.n2k.AISStaticData;

public class N2KAISStaticDataB implements AISStaticData {

    public N2KAISStaticDataB_PartA getPartA() {
        return partA;
    }

    public void setPartA(N2KAISStaticDataB_PartA partA) {
        this.partA = partA;
    }

    public N2KAISStaticDataB_PartB getPartB() {
        return partB;
    }

    public void setPartB(N2KAISStaticDataB_PartB partB) {
        this.partB = partB;
    }

    private N2KAISStaticDataB_PartA partA;
    private N2KAISStaticDataB_PartB partB;

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
    public String getRepeatIndicator() {
        if (partA != null) return partA.getRepeatIndicator();
        else if (partB != null) return partB.getRepeatIndicator();
        else return null;
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
        return "A";
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
