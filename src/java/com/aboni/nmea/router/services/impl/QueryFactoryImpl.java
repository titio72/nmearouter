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

package com.aboni.nmea.router.services.impl;

import com.aboni.nmea.router.services.QueryFactory;
import com.aboni.nmea.router.services.ServiceConfig;
import com.aboni.nmea.router.utils.Query;
import com.aboni.nmea.router.utils.QueryByDate;
import com.aboni.nmea.router.utils.QueryById;

import javax.inject.Inject;
import java.time.Instant;

public class QueryFactoryImpl implements QueryFactory {

    @Inject
    public QueryFactoryImpl() {
        // do nothing
    }

    @Override
    public Query getQuery(ServiceConfig config) {
        final int trip = config.getInteger("trip", -1);
        if (trip != -1) {
            return new QueryById(trip);
        } else {
            Instant cFrom = config.getParamAsInstant("from", Instant.now().minusSeconds(86400), 0);
            Instant cTo = config.getParamAsInstant("to", cFrom.plusSeconds(86401), 0);
            return new QueryByDate(cFrom, cTo);
        }
    }
}
