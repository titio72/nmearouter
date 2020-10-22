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

import com.aboni.misc.Utils;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.utils.DataEvent;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

public class SpeedAndHeadingStream {

    public interface SpeedAndHeadingListener {
        void onSpeedAndHeading(MsgSpeedAndHeading msg);
    }

    private SpeedAndHeadingListener listener;
    private DataEvent<MsgHeading> lastHeading;
    private final TimestampProvider timestampProvider;


    @Inject
    public SpeedAndHeadingStream(@NotNull TimestampProvider tp) {
        this.timestampProvider = tp;
    }

    public void setListener(SpeedAndHeadingListener listener) {
        this.listener = listener;
    }

    public void onMessage(Message m) {
        if (listener != null) {
            if (m instanceof MsgSpeed && lastHeading != null &&
                    !Utils.isOlderThan(lastHeading.getTimestamp(), timestampProvider.getNow(), 600)) {
                MsgSpeedAndHeadingFacade res = new MsgSpeedAndHeadingFacade((MsgSpeed) m, lastHeading.getData());
                listener.onSpeedAndHeading(res);
            } else if (m instanceof MsgHeading) {
                lastHeading = new DataEvent<>((MsgHeading) m, timestampProvider.getNow(), "");
            }
        }
    }
}
