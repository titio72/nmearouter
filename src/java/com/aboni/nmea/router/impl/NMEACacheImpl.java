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

package com.aboni.nmea.router.impl;

import com.aboni.nmea.router.NMEACache;
import com.aboni.nmea.router.TimestampProvider;
import com.aboni.nmea.router.message.*;
import com.aboni.nmea.router.utils.DataEvent;
import com.aboni.nmea.router.utils.Log;
import com.aboni.utils.LogStringBuilder;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class NMEACacheImpl implements NMEACache {


    private final Log log;
    private final TimestampProvider timestampProvider;
    private DataEvent<MsgHeading> lastHeading;
    private DataEvent<MsgPosition> lastPosition;
    private DataEvent<MsgSOGAdCOG> lastVector;
    private final Map<String, Object> statuses;

    @Inject
    public NMEACacheImpl(@NotNull Log log, @NotNull TimestampProvider tp) {
        lastHeading = new DataEvent<>(null, 0, "");
        lastPosition = new DataEvent<>(null, 0, "");
        statuses = new HashMap<>();
        this.log = log;
        this.timestampProvider = tp;
    }

    @Override
    public void onSentence(Message s, String src) {
        try {
            if (s instanceof MsgHeading && !((MsgHeading)s).isTrueHeading()) {
                // save magnetic heading
                lastHeading = new DataEvent<>((MsgHeading) s, timestampProvider.getNow(), src);
            } else if (s instanceof MsgPositionAndVector && ((MsgPositionAndVector) s).getPosition()!=null) {
                lastPosition = new DataEvent<>((MsgPosition) s, timestampProvider.getNow(), src);
                lastVector = new DataEvent<>((MsgSOGAdCOG) s, timestampProvider.getNow(), src);
            }
        } catch (Exception e) {
            log.error(() -> LogStringBuilder.start("Cache").wO("cache sentence").wV("sentence", s).toString(), e);
        }
    }

    @Override
    public DataEvent<MsgHeading> getLastHeading() {
        return lastHeading;
    }

    @Override
    public DataEvent<MsgPosition> getLastPosition() {
        return lastPosition;
    }

    @Override
    public DataEvent<MsgSOGAdCOG> getLastVector() {
        return lastVector;
    }

    @Override
    public <T> void setStatus(String statusKey, T status) {
        synchronized (statuses) {
            statuses.put(statusKey, status);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getStatus(String statusKey, T defaultValue) {
        synchronized (statuses) {
            return (T) statuses.getOrDefault(statusKey, defaultValue);
        }
    }
}