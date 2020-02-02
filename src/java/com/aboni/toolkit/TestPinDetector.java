package com.aboni.toolkit;

import com.aboni.sensors.EngineDetector;

public class TestPinDetector {

    public static void main(String[] args) {
        try {
            EngineDetector e = EngineDetector.getInstance();
            boolean s = false;
            while (true) {
                Thread.sleep(500);
                e.refresh();
                ;
                if (s != e.isEngineOn()) {
                    s = e.isEngineOn();
                    System.out.println(s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
