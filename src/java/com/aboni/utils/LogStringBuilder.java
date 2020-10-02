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

package com.aboni.utils;

public class LogStringBuilder {

    public static final String BLANK = " ";

    public static LogStringBuilder start(String category) {
        return new LogStringBuilder().withCategory(category);
    }

    private String category;
    private String operation;
    private String first = "";

    private final StringBuilder messageBuilder = new StringBuilder();

    public LogStringBuilder withCategory(String category) {
        this.category = category;
        return this;
    }

    public LogStringBuilder withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public LogStringBuilder withValue(String key, String value) {
        messageBuilder.append(String.format("%s%s {%s}", first, key, value));
        first = BLANK;
        return this;
    }

    public LogStringBuilder withValue(String key, double value) {
        messageBuilder.append(String.format("%s%s {%f}", first, key, value));
        first = BLANK;
        return this;
    }

    public LogStringBuilder withValue(String key, String fmt, double value) {
        return withValue(key, String.format(fmt, value));
    }

    public LogStringBuilder withValue(String key, String fmt, int value) {
        return withValue(key, String.format(fmt, value));
    }

    public LogStringBuilder withValue(String key, int value) {
        messageBuilder.append(String.format("%s%s {%d}", first, key, value));
        first = BLANK;
        return this;
    }

    public LogStringBuilder withValue(String key, String fmt, long value) {
        return withValue(key, String.format(fmt, value));
    }

    public LogStringBuilder withValue(String key, long value) {
        messageBuilder.append(String.format("%s%s {%d}", first, key, value));
        first = BLANK;
        return this;
    }

    public LogStringBuilder withValue(String key, Object value) {
        return withValue(key, "" + value);
    }

    public String get() {
        return String.format("%s: Op {%s} %s", category, operation, messageBuilder.toString());
    }

    @Override
    public String toString() {
        return get();
    }
}
