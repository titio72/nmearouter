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

import javax.validation.constraints.NotNull;

public class LogStringBuilder {

    public static final String BLANK = " ";

    public static LogStringBuilder start(String category) {
        return new LogStringBuilder().wC(category);
    }

    private String category;
    private String operation;
    private String first = "";

    private final StringBuilder messageBuilder = new StringBuilder();

    /**
     * Add a "category" to the log line.
     * <Category>: Op {<Operation>} ...
     *
     * @param category The string for the category.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wC(String category) {
        this.category = category;
        return this;
    }

    /**
     * Add an "operation" to the log line.
     * <Category>: Op {<Operation>} ...
     *
     * @param operation The string for the operation.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wO(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, String value) {
        messageBuilder.append(String.format("%s%s {%s}", first, key, value));
        first = BLANK;
        return this;
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, double value) {
        messageBuilder.append(String.format("%s%s {%f}", first, key, value));
        first = BLANK;
        return this;
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param fmt   The format (as in String.format(...)).
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, String fmt, double value) {
        return wV(key, String.format(fmt, value));
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param fmt   The format (as in String.format(...)).
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, String fmt, int value) {
        return wV(key, String.format(fmt, value));
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, int value) {
        messageBuilder.append(String.format("%s%s {%d}", first, key, value));
        first = BLANK;
        return this;
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param fmt   The format (as in String.format(...)).
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, String fmt, long value) {
        return wV(key, String.format(fmt, value));
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, long value) {
        messageBuilder.append(String.format("%s%s {%d}", first, key, value));
        first = BLANK;
        return this;
    }

    /**
     * Add a value to the log line.
     * <Category>: Op {<Operation>} <key> {<value>} ...
     *
     * @param key   The identifier to the value to be traced.
     * @param value The value to be traced.
     * @return The original LogStringBuilder for fluent interface.
     */
    public LogStringBuilder wV(String key, Object value) {
        return wV(key, "" + value);
    }

    public String get() {
        return String.format("%s: Op {%s} %s", category, operation, messageBuilder.toString());
    }

    @Override
    public String toString() {
        return get();
    }

    public void info(@NotNull Log log) {
        log.info(get());
    }

    public void error(@NotNull Log log) {
        log.error(get());
    }

    public void error(@NotNull Log log, Exception e) {
        log.error(get(), e);
    }

    public void errorForceStacktrace(@NotNull Log log, Exception e) {
        log.errorForceStacktrace(get(), e);
    }

    public void warn(@NotNull Log log) {
        log.warning(get());
    }

    public void warn(@NotNull Log log, Exception e) {
        log.warning(get(), e);
    }

    public LogStringBuilder w(String s) {
        messageBuilder.append(s);
        return this;
    }
}
