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

import org.json.JSONObject;

public class JSONUtils {

    private JSONUtils() {
    }

    public static boolean addDouble(JSONObject json, double v, String attribute) {
        if (Double.isNaN(v) || json == null || attribute == null || attribute.isEmpty()) {
            return false;
        } else {
            json.put(attribute, v);
            return true;
        }
    }

    public static void main(String[] args) {
        JSONObject o = new JSONObject();
        addDouble(o, Double.NaN, "a");
        double d = o.optDouble("a", Double.NaN);
        System.out.println(o.toString());
        System.out.printf("%03.1f", d);

    }
}
