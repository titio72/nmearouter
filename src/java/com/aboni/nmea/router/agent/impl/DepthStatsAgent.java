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

import com.aboni.log.Log;
import com.aboni.nmea.message.MsgWaterDepth;
import com.aboni.nmea.router.OnRouterMessage;
import com.aboni.nmea.router.RouterMessage;
import com.aboni.nmea.router.RouterMessageFactory;
import com.aboni.utils.TimestampProvider;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.Deque;
import java.util.LinkedList;

public class DepthStatsAgent extends NMEAAgentImpl {

    private static class DepthT {
        int depth;
        long timestamp;
    }

    private final Deque<DepthT> queue;

    private int max = Integer.MIN_VALUE;
    private int min = Integer.MAX_VALUE;

    private static final long DEFAULT_WINDOW = 60L * 60L * 1000L; // 1 hour

    @Inject
    public DepthStatsAgent(Log log, TimestampProvider tp, RouterMessageFactory messageFactory) {
        super(log, tp, messageFactory, true, true);
        queue = new LinkedList<>();
    }

    @Override
    public String getType() {
        return "Depth stats";
    }

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public String getDescription() {
        return "Max and min depth over the last hour" +
                ((min == Integer.MAX_VALUE) ? "" : String.format(" %.1f", min / 10f)) +
                ((max == Integer.MIN_VALUE) ? "" : String.format(" %.1f", max / 10f));
    }

    @OnRouterMessage
    public void onMessage(RouterMessage msg) {
        if (msg.getPayload() instanceof MsgWaterDepth) {
            DepthT d = handleDepth(((MsgWaterDepth) msg.getPayload()).getDepth(), getTimestampProvider().getNow());

            JSONObject j = new JSONObject();
            j.put("topic", "depth_stats");
            j.put("depth", d.depth / 10f);
            if (min != Integer.MAX_VALUE) j.put("min_1h", min / 10f);
            if (max != Integer.MIN_VALUE) j.put("max_1h", max / 10f);
            postMessage(j);
        }
    }

    /**
     * For testing purposes only
     * @param d The value of the depth
     * @param ts The timestamp (unix time) of the reading
     */
    public void privatePushDepth(double d, long ts) {
        handleDepth(d, ts);
    }

    private DepthT handleDepth(double depth, long ts) {
        DepthT d = new DepthT();
        d.depth = (int)(depth*10f);
        d.timestamp = ts;

        if (depth>0.1) {
            queue.add(d);


            max = Math.max(d.depth, max);
            min = Math.min(d.depth, min);

            boolean dirty = false;
            boolean goon = true;
            synchronized (this) {
                while (goon) {
                    goon = false;
                    DepthT d0 = queue.getFirst();
                    if ((d.timestamp -d0.timestamp)>DEFAULT_WINDOW) {
                        DepthT dd = queue.pop();
                        dirty = dirty || (!(dd.depth<max && dd.depth>min));
                        goon = true;
                    }
                }
            }

            if (dirty) {
                calcMinMax();
            }
        }
        return d;
    }

    private void calcMinMax() {
        synchronized (this) {
            max = Integer.MIN_VALUE;
            min = Integer.MAX_VALUE;
            for (DepthT d : queue) {
                max = Math.max(d.depth, max);
                min = Math.min(d.depth, min);
            }
        }
    }
}
