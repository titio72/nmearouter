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
