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

package com.aboni.nmea.router.message;

import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.data.DataEvent;
import com.aboni.utils.Utils;

import javax.inject.Inject;

public class PositionAndVectorStream {

    public interface PositionAndVectorListener {
        void onPosAndVector(MsgPositionAndVector msg);
    }

    private PositionAndVectorListener listener;
    private DataEvent<MsgSOGAdCOG> lastVector;
    private final TimestampProvider timestampProvider;


    @Inject
    public PositionAndVectorStream(TimestampProvider tp) {
        if (tp==null) throw new IllegalArgumentException("Timestamp provider is null");
        this.timestampProvider = tp;
    }

    public void setListener(PositionAndVectorListener listener) {
        this.listener = listener;
    }

    public void onMessage(Message m) {
        if (listener != null) {
            if (m instanceof MsgGNSSPosition && lastVector != null &&
                    !Utils.isOlderThan(lastVector.getTimestamp(), timestampProvider.getNow(), 600)) {
                MsgPositionAndVectorFacade res = new MsgPositionAndVectorFacade((MsgGNSSPosition) m, lastVector.getData());
                listener.onPosAndVector(res);
            } else if (m instanceof MsgSOGAdCOG) {
                lastVector = new DataEvent<>((MsgSOGAdCOG) m, timestampProvider.getNow(), "");
            }
        }
    }
}
