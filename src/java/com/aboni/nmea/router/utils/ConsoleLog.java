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

package com.aboni.nmea.router.utils;

import com.aboni.utils.LogFormatter;

import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConsoleLog implements Log {

    private final Logger lgConsole;

    private static final ConsoleLog logger = new ConsoleLog();

    public static ConsoleLog getLogger() {
        return logger;
    }

    private ConsoleLog() {
        lgConsole = Logger.getLogger("NMEAConsole");
        lgConsole.setLevel(Level.INFO);
        lgConsole.setUseParentHandlers(false);
        ConsoleHandler c = new ConsoleHandler();
        c.setFormatter(new LogFormatter());
        lgConsole.addHandler(c);
    }

    @Override
    public boolean isDebug() {
        return Level.FINEST == lgConsole.getLevel();
    }

    public void setDebug() {
        lgConsole.setLevel(Level.FINEST);
    }

    public void setError() {
        lgConsole.setLevel(Level.SEVERE);
    }

    public void setWarning() {
        lgConsole.setLevel(Level.WARNING);
    }

    public void setNone() {
        lgConsole.setLevel(Level.OFF);
    }

    public void setInfo() {
        lgConsole.setLevel(Level.INFO);
    }

    @Override
    public void error(String msg) {
        lgConsole.log(Level.SEVERE, msg);
    }

    @Override
    public void error(Supplier<String> msg) {
        if (lgConsole.getLevel().intValue() <= Level.SEVERE.intValue()) error(msg.get());
    }

    @Override
    public void error(final String msg, final Throwable t) {
        lgConsole.log(Level.SEVERE, msg, t);
    }

    @Override
    public void error(Supplier<String> supplier, Throwable t) {
        if (lgConsole.getLevel().intValue() <= Level.SEVERE.intValue()) error(supplier.get(), t);
    }

    @Override
    public void errorForceStacktrace(final String msg, final Throwable t) {
        lgConsole.log(Level.SEVERE, msg, t);
    }

    @Override
    public void errorForceStacktrace(Supplier<String> supplier, Throwable t) {
        if (lgConsole.getLevel().intValue() <= Level.SEVERE.intValue()) errorForceStacktrace(supplier.get(), t);
    }

    @Override
    public void warning(String msg) {
        lgConsole.log(Level.WARNING, msg);
    }

    @Override
    public void warning(Supplier<String> msg) {
        if (lgConsole.getLevel().intValue() <= Level.WARNING.intValue()) warning(msg.get());
    }

    @Override
    public void warning(String msg, Exception e) {
        lgConsole.warning(() -> String.format("{%s} error {%s}", msg, e.getMessage()));
    }

    @Override
    public void warning(Supplier<String> msg, Exception e) {
        if (lgConsole.getLevel().intValue() <= Level.WARNING.intValue()) warning(msg.get(), e);
    }

    @Override
    public void info(String msg) {
        lgConsole.log(Level.INFO, msg);
    }

    @Override
    public void info(Supplier<String> msg) {
        if (lgConsole.getLevel().intValue() <= Level.INFO.intValue()) info(msg.get());
    }

    @Override
    public void infoFill(String msg) {
        lgConsole.log(Level.INFO, () -> fill(msg));
    }

    @Override
    public void debug(String msg) {
        lgConsole.log(Level.FINER, msg);
    }

    private static final String FILLER = "--------------------------------------------------------------------------------";
    private static final String FILLER_LEFT = "---";

    private static String fill(String msg) {
        if (msg == null || msg.isEmpty()) return FILLER;
        return (FILLER_LEFT + " " + msg + " " + FILLER).substring(0, 80);
    }
}
