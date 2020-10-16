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

package com.aboni.nmea.router.conf;

import org.json.JSONObject;

public class SimpleConf implements AgentConfJSON {

    private final String name;
    private final String type;
    private final JSONObject cfg = new JSONObject();

    public SimpleConf(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public SimpleConf setAttribute(String key, int value) {
        cfg.put(key, value);
        return this;
    }

    public SimpleConf setAttribute(String key, String value) {
        cfg.put(key, value);
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public QOS getQos() {
        return null;
    }

    @Override
    public JSONObject getConfiguration() {
        return cfg;
    }
}
