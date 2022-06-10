/*
 * Copyright (c) 2022,  Andrea Boni
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

package com.aboni.nmea.router.data.meteo.impl;

import com.aboni.nmea.router.conf.MalformedConfigurationException;
import com.aboni.nmea.router.data.DataManagementException;
import com.aboni.nmea.router.data.DataReader;
import com.aboni.nmea.router.data.Sample;
import com.aboni.utils.db.DBHelper;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class DBMeteoReaderInflux implements DataReader {

    private static final String SQL_TIME = "select time, type, value, valueMax, valueMin from meteo where time>=$startTime and time<$endTime";
    private static final String SQL_TIME_AND_TYPE = "select time, type, value, valueMax, valueMin from meteo where time>=$startTime and time<$endTime and type=$type";

    @Override
    public void readData(@NotNull Instant from, @NotNull Instant to, @NotNull DataReader.DataReaderListener target) throws DataManagementException {
        try (DBHelper db = new DBHelper(true)) {
            Query query = BoundParameterQuery.QueryBuilder.newQuery(SQL_TIME)
                    .forDatabase("nmearouter")
                    .bind("startTime", from.toEpochMilli())
                    .bind("endTime", to.toEpochMilli())
                    .create();
            handleResult(target, db, query);
        } catch (ClassNotFoundException | MalformedConfigurationException e) {
            throw new DataManagementException("Error reading meteo", e);
        }
    }

    private void handleResult(DataReaderListener target, DBHelper db, Query query) throws DataManagementException {
        QueryResult queryResult = db.getInflux().query(query);
        for (QueryResult.Result r : queryResult.getResults()) {
            target.onRead(getSample(r));
        }
        if (queryResult.hasError()) {
            throw new DataManagementException("Error reading meteo: " + queryResult + queryResult.getError());
        }
    }

    private Sample getSample(QueryResult.Result r) {
        // todo
        return null;
    }

    @Override
    public void readData(@NotNull Instant from, @NotNull Instant to, @NotNull String tag, @NotNull DataReader.DataReaderListener target) throws DataManagementException {
        try (DBHelper db = new DBHelper(true)) {
            Query query = BoundParameterQuery.QueryBuilder.newQuery(SQL_TIME_AND_TYPE)
                    .forDatabase("nmearouter")
                    .bind("startTime", from.toEpochMilli())
                    .bind("endTime", to.toEpochMilli())
                    .bind("type", tag)
                    .create();
            handleResult(target, db, query);
        } catch (ClassNotFoundException | MalformedConfigurationException e) {
            throw new DataManagementException("Error reading meteo", e);
        }
    }
}
