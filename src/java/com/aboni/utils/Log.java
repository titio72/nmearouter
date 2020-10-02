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

package com.aboni.utils;

import java.util.function.Supplier;

public interface Log {

    boolean isDebug();

    void error(String msg);

    void error(String msg, Throwable t);

    void errorForceStacktrace(String msg, Throwable t);

    void warning(String msg);

    void warning(String msg, Exception e);

    void info(String msg);

    void infoFill(String msg);

    void debug(String msg);

    default void debug(Supplier<String> supplier) {
        if (isDebug()) debug(supplier.get());
    }

    void console(String msg);


}