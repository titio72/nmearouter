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

package com.aboni.nmea.router.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

public interface ServiceConfig {

    String getParameter(String param);

    String getParameter(String param, String defaultValue);

    String dump();

    default int getInteger(String param, int defaultValue) {
        String p = getParameter(param);
        if (p != null) {
            try {
                return Integer.parseInt(p);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    default double getDouble(String param, double defaultValue) {
        String p = getParameter(param);
        if (p != null) {
            try {
                return Double.parseDouble(p);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    default LocalDate getParamAsDate(String param, LocalDate def) {
        String f = getParameter(param);
        if (f == null || f.length() == 0) {
            return def;
        } else {
            try {
                return LocalDate.parse(f, DateTimeFormatter.ofPattern("yyyyMMdd"));
            } catch (DateTimeParseException e) {
                return def;
            }
        }
    }

    default Instant getParamAsInstant(String param, Instant def, int offset) {
        String f = getParameter(param);
        if (f == null || f.length() == 0) {
            return def;
        } else {
            try {
                DateTimeFormatter p = new DateTimeFormatterBuilder()
                        .appendValue(ChronoField.YEAR, 4)
                        .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                        .appendValue(ChronoField.DAY_OF_MONTH, 2)
                        .appendPattern("HHmmssZZ")
                        .toFormatter();
                OffsetDateTime d = OffsetDateTime.parse(f, p);
                return d.plusDays(offset).toInstant();
            } catch (DateTimeParseException ignored) {
                return def;
            }
        }
    }
}
