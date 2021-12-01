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
import net.sf.marineapi.nmea.util.Position;
import org.json.JSONObject;

import java.time.Instant;

public interface MsgPositionAndVector extends MsgPosition, MsgSOGAdCOG {

    Instant getTimestamp();

    double getVariation();

    default boolean isValid() {
        return getPosition() != null && !Double.isNaN(getCOG()) && !Double.isNaN(getSOG()) && getTimestamp() != null;
    }

    default GNSSInfo getGNSSInfo() {
        return new GNSSInfo() {
            @Override
            public Position getPosition() {
                return MsgPositionAndVector.this.getPosition();
            }

            @Override
            public double getCOG() {
                return MsgPositionAndVector.this.getCOG();
            }

            @Override
            public double getSOG() {
                return MsgPositionAndVector.this.getSOG();
            }
        };
    }


    @Override
    default JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("topic", "RMC");
        if (getTimestamp() != null) j.put("UTC", getTimestamp().toString());
        if (!Double.isNaN(getCOG())) j.put("COG", getCOG());
        if (!Double.isNaN(getSOG())) j.put("SOG", getSOG());
        if (getPosition() != null) {
            j.put("latitude", Utils.formatLatitude(getPosition().getLatitude()));
            j.put("longitude", Utils.formatLongitude(getPosition().getLongitude()));
            j.put("dec_latitude", getPosition().getLatitude());
            j.put("dec_longitude", getPosition().getLongitude());
        }
        return j;
    }

    @Override
    default String getMessageContentType() {
        return "PosAndVector";
    }
}
