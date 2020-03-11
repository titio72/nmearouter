package com.aboni.nmea.router.agent;

import com.aboni.nmea.router.Startable;

public interface NMEAAgent extends Startable {

    String getType();

    String getName();

    String getDescription();

    void setup(String name, QOS qos);

    boolean isBuiltIn();

    boolean isUserCanStartAndStop();

    void setStatusListener(NMEAAgentStatusListener listener);

    NMEASource getSource();

    NMEATarget getTarget();

    void onTimer();
	void onTimerHR();
}
