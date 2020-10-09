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

package com.aboni.nmea.router.agent.impl;

import com.aboni.nmea.router.AutoPilotDriver;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.n2k.PilotMode;
import com.aboni.utils.Log;
import net.sf.marineapi.nmea.parser.SentenceFactory;
import net.sf.marineapi.nmea.sentence.STALKSentence;
import net.sf.marineapi.nmea.sentence.SentenceId;
import net.sf.marineapi.nmea.sentence.TalkerId;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

/**
 *
 * @author aboni
 *
 * 86  X1  YY  yy  Keystroke 
X=1: Sent by Z101 remote control to increment/decrement
course of autopilot
11  05  FA     -1
11  06  F9    -10
11  07  F8     +1
11  08  F7    +10
11  20  DF     +1 &  -1
11  21  DE     -1 & -10
11  22  DD     +1 & +10
11  28  D7    +10 & -10
11  45  BA     -1        pressed longer than 1 second
11  46  B9    -10        pressed longer than 1 second
11  47  B8     +1        pressed longer than 1 second
11  48  B7    +10        pressed longer than 1 second
11  60  DF     +1 &  -1  pressed longer than 1 second
11  61  9E     -1 & -10  pressed longer than 1 second
11  62  9D     +1 & +10  pressed longer than 1 second
11  64  9B    +10 & -10  pressed longer than 1 second (why not 11 68 97 ?)

Sent by autopilot (X=0: ST 1000+,  X=2: ST4000+ or ST600R)
X1  01  FE    Auto
X1  02  FD    Standby
X1  03  FC    Track
X1  04  FB    disp (in display mode or page in auto chapter = advance)
X1  05  FA     -1 (in auto mode)
X1  06  F9    -10 (in auto mode)
X1  07  F8     +1 (in auto mode)
X1  08  F7    +10 (in auto mode)
X1  09  F6     -1 (in resp or rudder gain mode)
X1  0A  F5     +1 (in resp or rudder gain mode)
X1  21  DE     -1 & -10 (port tack, doesn�t work on ST600R?)
X1  22  DD     +1 & +10 (stb tack)
X1  23  DC    Standby & Auto (wind mode)
X1  28  D7    +10 & -10 (in auto mode)
X1  2E  D1     +1 & -1 (Response Display)
X1  41  BE    Auto pressed longer
X1  42  BD    Standby pressed longer
X1  43  BC    Track pressed longer
X1  44  BB    Disp pressed longer
X1  45  BA     -1 pressed longer (in auto mode)
X1  46  B9    -10 pressed longer (in auto mode)
X1  47  B8     +1 pressed longer (in auto mode)
X1  48  B7    +10 pressed longer (in auto mode)
X1  63  9C    Standby & Auto pressed longer (previous wind angle)
X1  68  97    +10 & -10 pressed longer (in auto mode)
X1  6E  91     +1 & -1 pressed longer (Rudder Gain Display)
X1  80  7F     -1 pressed (repeated 1x per second)
X1  81  7E     +1 pressed (repeated 1x per second)
X1  82  7D    -10 pressed (repeated 1x per second)
X1  83  7C    +10 pressed (repeated 1x per second)
X1  84  7B     +1, -1, +10 or -10 released
 *
 *
 */

public class NMEAAutoPilotAgent extends NMEAAgentImpl implements AutoPilotDriver {

    public static final String CHANGE_STATUS_OP = "change status";
    public static final String NEW_STATUS_KEY_NAME = "new status";

    private final Log log;

    @Inject
    public NMEAAutoPilotAgent(@NotNull Log log, @NotNull TimestampProvider tp) {
        super(log, tp, false, false);
        this.log = log;
    }

    @Override
    public String getType() {
        return "SmartPilot";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Raymarine SeaTalk autopilot driver";
    }

    @Override
    protected boolean onActivate() {
        return true;
    }


    @Override
    public void enable() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("21", "01", "FE");
        getLogBuilder().wO(CHANGE_STATUS_OP).wV(NEW_STATUS_KEY_NAME, "auto").info(log);
        this.notify(s);
    }

    @Override
    public void standBy() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("21", "02", "FD");
        getLogBuilder().wO(CHANGE_STATUS_OP).wV(NEW_STATUS_KEY_NAME, "stand by").info(log);
        this.notify(s);
    }

    @Override
    public void windVane() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("21", "23", "DC");
        getLogBuilder().wO(CHANGE_STATUS_OP).wV(NEW_STATUS_KEY_NAME, "vane").info(log);
        this.notify(s);
    }
    //11  05  FA     -1
    //11  06  F9    -10 
    //11  07  F8     +1 
    //11  08  F7    +10 
    // X1  05  FA     -1 (in auto mode)
    // X1  06  F9    -10 (in auto mode)
    // X1  07  F8     +1 (in auto mode)
    // X1  08  F7    +10 (in auto mode)

    private void logDirection(boolean port, int deg) {
        getLogBuilder().wO("steer").wV("direction", port ? "port" : "starboard").wV("degrees", deg).info(log);
    }

    @Override
    public void port1() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("11", "05", "FA");
        logDirection(true, 1);
        this.notify(s);
    }


    @Override
    public void port10() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("11", "06", "F9");
        logDirection(true, 10);
        this.notify(s);
    }

    @Override
    public void starboard1() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("11", "07", "F8");
        logDirection(false, 1);
        this.notify(s);
    }

    @Override
    public void starboard10() {
        STALKSentence s = (STALKSentence) SentenceFactory.getInstance().createParser(TalkerId.ST, SentenceId.ALK);
        s.setCommand("86");
        s.setParameters("11", "08", "F7");
        logDirection(false, 10);
        this.notify(s);
    }

    @Override
    public PilotMode getMode() {
        return null;
    }

    @Override
    public JSONObject getModeDescription() {
        return null;
    }
}
