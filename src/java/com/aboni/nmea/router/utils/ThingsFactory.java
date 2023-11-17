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

package com.aboni.nmea.router.utils;

import com.aboni.log.Log;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ThingsFactory {

    private ThingsFactory() {
    }

    private static Injector injector;

    public static void setInjector(Injector injector) {
        ThingsFactory.injector = injector;
    }

    public static <T> T getInstance(Class<T> aClass) {
        return injector.getInstance(aClass);
    }

    public static <T> T getInstance(Class<T> aClass, String named) {
        Key<T> key = Key.get(aClass, Names.named(named));
        return injector.getInstance(key);
    }

    public static <T> T getInstance(Class<T> aClass, Log logger) {
        T res = getInstance(aClass);
        if (res != null) {
            try {
                Method m = res.getClass().getMethod("setLogger", Log.class);
                m.invoke(res, logger);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // ignored
            }
        }
        return res;
    }
}