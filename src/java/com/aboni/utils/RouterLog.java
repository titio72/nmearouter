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

import com.aboni.nmea.router.Constants;

import java.io.IOException;
import java.util.logging.*;

public class RouterLog implements LogAdmin {
    private boolean debug = false;

    private final Logger lg;

    public RouterLog() {
        lg = Logger.getLogger(Constants.LOG_CONTEXT);
        lg.setLevel(Level.INFO);

        // disable global logger
        Logger.getGlobal().setLevel(Level.OFF);

        FileHandler fh;
        try {
            lg.setUseParentHandlers(false);

            fh = new FileHandler(Constants.LOG, 0, 1, true);
            lg.addHandler(fh);

            Formatter formatter = new LogFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException | IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "Error", e);
        }
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public void setDebug() {
        lg.setLevel(Level.FINEST);
        debug = true;
    }

    @Override
    public void setError() {
        lg.setLevel(Level.SEVERE);
        debug = false;
    }

    @Override
    public void setWarning() {
        lg.setLevel(Level.WARNING);
        debug = false;
    }

    @Override
    public void setNone() {
        lg.setLevel(Level.OFF);
        debug = false;
    }

    @Override
    public void setInfo() {
        lg.setLevel(Level.INFO);
        debug = false;
    }

    @Override
    public void error(String msg) {
        lg.log(Level.SEVERE, msg);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        if (debug)
            lg.log(Level.SEVERE, msg, t);
        else
            lg.severe(() -> String.format("%s error {%s}", msg, t.getMessage()));
    }

    @Override
    public void errorForceStacktrace(final String msg, final Throwable t) {
        lg.log(Level.SEVERE, msg, t);
    }

    @Override
    public void warning(String msg) {
        lg.log(Level.WARNING, msg);
    }

    @Override
    public void warning(String msg, Exception e) {
        if (debug)
            lg.log(Level.WARNING, msg, e);
        else
            lg.warning(() -> String.format("%s error {%s}", msg, e.getMessage()));
    }

    @Override
    public void info(String msg) {
        lg.log(Level.INFO, msg);
    }

    @Override
    public void infoFill(String msg) {
        lg.log(Level.INFO, () -> fill(msg));
    }

    @Override
    public void debug(String msg) {
        lg.log(Level.FINER, msg);
    }

    @Override
    public Logger getBaseLogger() {
        return lg;
    }

    private static final String FILLER = "--------------------------------------------------------------------------------";
    private static final String FILLER_LEFT = "---";

    private static String fill(String msg) {
        if (msg == null || msg.isEmpty()) return FILLER;
        return (FILLER_LEFT + " " + msg + " " + FILLER).substring(0, 80);
    }
}
